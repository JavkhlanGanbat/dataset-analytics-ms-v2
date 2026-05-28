import type { ColumnProfile, DatasetProfile } from '../model/datasetTypes';
import {
  formatPercent,
  getColumnSummary,
  getColumnWarnings,
  getInsightCategory,
  getQualityGradeClass
} from '../model/profilePresentation';

type ProfileViewProps = {
  profile: DatasetProfile;
};

export function ProfileView({ profile }: ProfileViewProps) {
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

      <div className={`quality-band ${getQualityGradeClass(profile.dataQuality.grade)}`}>
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
              <span>{getInsightCategory(insight)}</span>
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
                  <td>{getColumnSummary(column)}</td>
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
  const warnings = getColumnWarnings(column);
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
