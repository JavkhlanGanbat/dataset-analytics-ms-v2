import { AlertCircle, BarChart3, CheckCircle2, RefreshCw, UploadCloud } from 'lucide-react';
import { FormEvent, useEffect, useRef, useState } from 'react';
import {
  ColumnProfile,
  DatasetProfile,
  DatasetSummary,
  fetchDatasetProfile,
  fetchDatasets,
  uploadDataset
} from './services/api';

type Notice = {
  type: 'success' | 'error';
  message: string;
};

function App() {
  const [datasetName, setDatasetName] = useState('');
  const [datasets, setDatasets] = useState<DatasetSummary[]>([]);
  const [selectedDatasetId, setSelectedDatasetId] = useState<number | null>(null);
  const [profile, setProfile] = useState<DatasetProfile | null>(null);
  const [uploadNotice, setUploadNotice] = useState<Notice | null>(null);
  const [listError, setListError] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isLoadingDatasets, setIsLoadingDatasets] = useState(false);
  const [isLoadingProfile, setIsLoadingProfile] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    loadDatasets();
  }, []);

  async function loadDatasets(nextSelectedId?: number) {
    setIsLoadingDatasets(true);
    setListError(null);
    try {
      const result = await fetchDatasets();
      setDatasets(result);
      if (nextSelectedId) {
        setSelectedDatasetId(nextSelectedId);
      } else if (selectedDatasetId == null && result.length > 0) {
        setSelectedDatasetId(result[0].id);
      }
    } catch (error) {
      setListError(error instanceof Error ? error.message : 'Failed to load datasets.');
    } finally {
      setIsLoadingDatasets(false);
    }
  }

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const file = fileInputRef.current?.files?.[0];

    if (!datasetName.trim() || !file) {
      setUploadNotice({ type: 'error', message: 'Dataset name and CSV file are required.' });
      return;
    }

    setIsUploading(true);
    setUploadNotice(null);
    try {
      const uploaded = await uploadDataset(datasetName.trim(), file);
      setDatasetName('');
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      setProfile(null);
      setUploadNotice({
        type: 'success',
        message: `Dataset uploaded successfully. "${uploaded.name}" is ready for analysis.`
      });
      await loadDatasets(uploaded.id);
    } catch (error) {
      setUploadNotice({ type: 'error', message: error instanceof Error ? error.message : 'Upload failed.' });
    } finally {
      setIsUploading(false);
    }
  }

  async function handleProfileLoad(datasetId = selectedDatasetId) {
    if (datasetId == null) {
      setProfileError('Select a dataset first.');
      return;
    }

    setIsLoadingProfile(true);
    setProfileError(null);
    try {
      const result = await fetchDatasetProfile(datasetId);
      setProfile(result);
    } catch (error) {
      setProfileError(error instanceof Error ? error.message : 'Failed to load profile.');
    } finally {
      setIsLoadingProfile(false);
    }
  }

  function selectDataset(datasetId: number) {
    setSelectedDatasetId(datasetId);
    setProfileError(null);
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <h1>DataProfiler</h1>
          <p className="subtitle">
            Upload a CSV file and instantly detect missing values, outliers, and quality issues.
          </p>
        </div>
      </header>

      <div className="workspace-grid">
        <section className="panel">
          <div className="section-heading">
            <h2>Upload CSV</h2>
          </div>
          <form className="upload-form" onSubmit={handleUpload}>
            <label>
              Dataset name
              <input
                value={datasetName}
                onChange={(event) => setDatasetName(event.target.value)}
                placeholder="Sales Dataset"
              />
            </label>
            <label>
              CSV file
              <input ref={fileInputRef} type="file" accept=".csv,text/csv" />
            </label>
            <button className="primary-button" type="submit" disabled={isUploading}>
              <UploadCloud size={18} aria-hidden="true" />
              {isUploading ? 'Uploading' : 'Upload'}
            </button>
          </form>
          {uploadNotice && (
            <StatusMessage type={uploadNotice.type} message={uploadNotice.message} />
          )}
        </section>

        <section className="panel dataset-panel">
          <div className="section-heading with-action">
            <h2>Datasets</h2>
            <button className="icon-button" type="button" onClick={() => loadDatasets()} disabled={isLoadingDatasets}>
              <RefreshCw size={17} aria-hidden="true" />
              Refresh
            </button>
          </div>
          {listError && <StatusMessage type="error" message={listError} />}
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Rows</th>
                  <th>Columns</th>
                  <th>Created</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {datasets.map((dataset) => (
                  <tr key={dataset.id} className={selectedDatasetId === dataset.id ? 'selected-row' : ''}>
                    <td>{dataset.id}</td>
                    <td>{dataset.name}</td>
                    <td>{dataset.rowCount}</td>
                    <td>{dataset.columnCount}</td>
                    <td>{formatDate(dataset.createdAt)}</td>
                    <td>
                      <button
                        className={`text-button ${selectedDatasetId === dataset.id ? 'selected-button' : ''}`}
                        type="button"
                        onClick={() => selectDataset(dataset.id)}
                        disabled={selectedDatasetId === dataset.id}
                      >
                        {selectedDatasetId === dataset.id ? 'Selected' : 'Select'}
                      </button>
                    </td>
                  </tr>
                ))}
                {!isLoadingDatasets && datasets.length === 0 && (
                  <tr>
                    <td colSpan={6} className="empty-cell">No datasets yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel analytics-panel">
          <div className="section-heading with-action">
            <h2>Dataset Profile</h2>
            <button
              className="primary-button compact"
              type="button"
              onClick={() => handleProfileLoad()}
              disabled={isLoadingProfile || selectedDatasetId == null}
            >
              <BarChart3 size={18} aria-hidden="true" />
              {isLoadingProfile ? 'Analyzing' : 'Generate Profile'}
            </button>
          </div>
          <label className="select-label">
            Dataset
            <select
              value={selectedDatasetId ?? ''}
              onChange={(event) => selectDataset(Number(event.target.value))}
            >
              <option value="" disabled>Select dataset</option>
              {datasets.map((dataset) => (
                <option key={dataset.id} value={dataset.id}>
                  {dataset.id} - {dataset.name}
                </option>
              ))}
            </select>
          </label>
          {profileError && <StatusMessage type="error" message={profileError} />}
          {profile ? <ProfileView profile={profile} /> : <div className="empty-profile">Choose a dataset and generate a profile.</div>}
        </section>
      </div>
    </main>
  );
}

function StatusMessage({ type, message }: Notice) {
  const Icon = type === 'success' ? CheckCircle2 : AlertCircle;
  return (
    <div className={`status-message ${type}`}>
      <Icon size={18} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}

function ProfileView({ profile }: { profile: DatasetProfile }) {
  return (
    <div className="profile-stack">
      <div className="profile-summary">
        <div>
          <span>Dataset</span>
          <strong>{profile.datasetName}</strong>
        </div>
        <div>
          <span>ID</span>
          <strong>{profile.datasetId}</strong>
        </div>
        <div>
          <span>Rows</span>
          <strong>{profile.totalRowCount}</strong>
        </div>
        <div>
          <span>Columns</span>
          <strong>{profile.totalColumnCount}</strong>
        </div>
      </div>

      <div className={`quality-band ${gradeClass(profile.dataQuality.grade)}`}>
        <div className="quality-score">
          <span>Data Quality Score</span>
          <strong>{profile.dataQuality.score}<small>/100</small></strong>
          <em>Grade {profile.dataQuality.grade}</em>
        </div>
        <p>{profile.dataQuality.explanation}</p>
      </div>

      <div>
        <h3>Key Findings</h3>
        <div className="insight-grid">
          {profile.insights.map((insight) => (
            <article className="insight-card" key={insight}>
              <span>{insightCategory(insight)}</span>
              <p>{insight}</p>
            </article>
          ))}
        </div>
      </div>

      <div>
        <h3>Column Details</h3>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Column</th>
                <th>Type</th>
                <th>Values</th>
                <th>Missing</th>
                <th>Distinct</th>
                <th>Warnings</th>
                <th>Summary</th>
              </tr>
            </thead>
            <tbody>
              {profile.columns.map((column) => (
                <tr key={column.columnName}>
                  <td>{column.columnName}</td>
                  <td><span className={`type-chip ${column.inferredType.toLowerCase()}`}>{column.inferredType}</span></td>
                  <td>{column.totalValues}</td>
                  <td>{column.missingValues} ({formatPercent(column.missingPercentage)})</td>
                  <td>{column.distinctCount}</td>
                  <td><WarningLabels column={column} /></td>
                  <td>{columnSummary(column)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function WarningLabels({ column }: { column: ColumnProfile }) {
  const warnings = columnWarnings(column);
  if (warnings.length === 0) {
    return <span className="muted-text">None</span>;
  }

  return (
    <div className="warning-list">
      {warnings.map((warning) => (
        <span className="warning-chip" key={warning}>{warning}</span>
      ))}
    </div>
  );
}

function columnWarnings(column: ColumnProfile) {
  const warnings: string[] = [];
  if (column.missingValues > 0) {
    warnings.push(`${formatPercent(column.missingPercentage)} missing`);
  }
  if (column.outlierCount > 0) {
    warnings.push(`${column.outlierCount} outlier${column.outlierCount === 1 ? '' : 's'}`);
  }
  if (column.inferredType === 'MIXED') {
    warnings.push('Mixed values');
  }
  if (column.inferredType === 'EMPTY') {
    warnings.push('Empty');
  }
  if (hasLowDiversity(column)) {
    warnings.push('Low diversity');
  }
  return warnings;
}

function hasLowDiversity(column: ColumnProfile) {
  const presentValues = column.totalValues - column.missingValues;
  return column.inferredType !== 'NUMERIC'
    && column.inferredType !== 'EMPTY'
    && presentValues >= 4
    && column.distinctCount > 0
    && column.distinctCount <= 2;
}

function columnSummary(column: ColumnProfile) {
  if (column.inferredType === 'NUMERIC' || column.numericValueCount > 0) {
    return numericSummary(column);
  }
  return textSummary(column);
}

function numericSummary(column: ColumnProfile) {
  if (column.inferredType !== 'NUMERIC' && column.numericValueCount === 0) {
    return '-';
  }

  return [
    `Min ${formatValue(column.min)}`,
    `Max ${formatValue(column.max)}`,
    `Mean ${formatValue(column.mean)}`,
    `Median ${formatValue(column.median)}`,
    `Std ${formatValue(column.standardDeviation)}`
  ].join(', ');
}

function textSummary(column: ColumnProfile) {
  const topValues = Object.entries(column.mostCommonValues ?? {})
    .map(([value, count]) => `${value} (${count})`)
    .join(', ');
  const averageLength = column.averageTextLength == null ? null : `avg length ${formatValue(column.averageTextLength)}`;
  return [topValues, averageLength].filter(Boolean).join('; ') || '-';
}

function insightCategory(insight: string) {
  const normalized = insight.toLowerCase();
  if (normalized.includes('outlier')) {
    return 'Possible outlier';
  }
  if (normalized.includes('missing')) {
    return 'Missing values';
  }
  if (normalized.includes('low diversity')) {
    return 'Low diversity';
  }
  if (normalized.includes('wide value range')) {
    return 'Wide range';
  }
  return 'Insight';
}

function gradeClass(grade: string) {
  if (grade === 'A') {
    return 'quality-a';
  }
  if (grade === 'B') {
    return 'quality-b';
  }
  if (grade === 'C') {
    return 'quality-c';
  }
  return 'quality-d';
}

function formatValue(value: number | null) {
  if (value == null) {
    return '-';
  }
  return Number.isInteger(value) ? String(value) : value.toFixed(2);
}

function formatPercent(value: number) {
  return `${value.toFixed(value % 1 === 0 ? 0 : 1)}%`;
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value));
}

export default App;
