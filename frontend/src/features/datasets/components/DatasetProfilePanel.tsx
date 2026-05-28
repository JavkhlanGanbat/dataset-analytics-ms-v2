import { BarChart3 } from 'lucide-react';

import type { DatasetProfile, DatasetSummary } from '../model/datasetTypes';
import { ProfileView } from './ProfileView';
import { StatusMessage } from './StatusMessage';

type DatasetProfilePanelProps = {
  datasets: DatasetSummary[];
  error: string | null;
  isLoading: boolean;
  profile: DatasetProfile | null;
  selectedDatasetId: number | null;
  onGenerateProfile: () => Promise<void>;
  onSelectDataset: (datasetId: number | null) => void;
};

export function DatasetProfilePanel({
  datasets,
  error,
  isLoading,
  profile,
  selectedDatasetId,
  onGenerateProfile,
  onSelectDataset
}: DatasetProfilePanelProps) {
  return (
    <section className="panel analytics-panel">
      <div className="section-heading with-action">
        <h2>Dataset Profile</h2>
        <button
          className="primary-button compact"
          type="button"
          onClick={() => void onGenerateProfile()}
          disabled={isLoading || selectedDatasetId == null}
        >
          <BarChart3 size={18} aria-hidden="true" />
          {isLoading ? 'Analyzing' : 'Generate Profile'}
        </button>
      </div>
      <label className="select-label">
        Dataset
        <select
          value={selectedDatasetId ?? ''}
          onChange={(event) => onSelectDataset(event.target.value === '' ? null : Number(event.target.value))}
        >
          <option value="" disabled>Select dataset</option>
          {datasets.map((dataset) => (
            <option key={dataset.id} value={dataset.id}>
              {dataset.id} - {dataset.name}
            </option>
          ))}
        </select>
      </label>
      {error && <StatusMessage type="error" message={error} />}
      {profile ? <ProfileView profile={profile} /> : <div className="empty-profile">Choose a dataset and generate a profile.</div>}
    </section>
  );
}
