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
import { API_GATEWAY_URL } from './services/config';

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
      setUploadNotice({ type: 'success', message: `Uploaded dataset ${uploaded.id}: ${uploaded.name}` });
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
          <p className="eyebrow">Microservices Demo</p>
          <h1>Dataset Analytics</h1>
        </div>
        <div className="gateway-pill">Gateway: {API_GATEWAY_URL}</div>
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
                      <button className="text-button" type="button" onClick={() => selectDataset(dataset.id)}>
                        Select
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
            <h2>Analytics</h2>
            <button
              className="primary-button compact"
              type="button"
              onClick={() => handleProfileLoad()}
              disabled={isLoadingProfile || selectedDatasetId == null}
            >
              <BarChart3 size={18} aria-hidden="true" />
              {isLoadingProfile ? 'Loading' : 'Profile'}
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
          {profile ? <ProfileView profile={profile} /> : <div className="empty-profile">No profile loaded.</div>}
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

      <div className="quality-band">
        <div className="quality-score">
          <span>{profile.dataQuality.grade}</span>
          <strong>{profile.dataQuality.score}</strong>
        </div>
        <p>{profile.dataQuality.explanation}</p>
      </div>

      <div>
        <h3>Insights</h3>
        <ul className="insight-list">
          {profile.insights.map((insight) => (
            <li key={insight}>{insight}</li>
          ))}
        </ul>
      </div>

      <div>
        <h3>Column Statistics</h3>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Column</th>
                <th>Type</th>
                <th>Values</th>
                <th>Missing</th>
                <th>Distinct</th>
                <th>Numeric stats</th>
                <th>Text stats</th>
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
                  <td>{numericSummary(column)}</td>
                  <td>{textSummary(column)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function numericSummary(column: ColumnProfile) {
  if (column.inferredType !== 'NUMERIC' && column.numericValueCount === 0) {
    return '-';
  }

  return [
    `min ${formatValue(column.min)}`,
    `max ${formatValue(column.max)}`,
    `mean ${formatValue(column.mean)}`,
    `median ${formatValue(column.median)}`,
    `std ${formatValue(column.standardDeviation)}`,
    `outliers ${column.outlierCount}`
  ].join(', ');
}

function textSummary(column: ColumnProfile) {
  const topValues = Object.entries(column.mostCommonValues ?? {})
    .map(([value, count]) => `${value} (${count})`)
    .join(', ');
  const averageLength = column.averageTextLength == null ? null : `avg length ${formatValue(column.averageTextLength)}`;
  return [topValues, averageLength].filter(Boolean).join('; ') || '-';
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
