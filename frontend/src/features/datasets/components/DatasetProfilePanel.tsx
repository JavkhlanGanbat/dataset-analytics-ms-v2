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
        <h2>Өгөгдлийн профайл</h2>
        <button
          className="primary-button compact"
          type="button"
          onClick={() => void onGenerateProfile()}
          disabled={isLoading || selectedDatasetId == null}
        >
          <BarChart3 size={18} aria-hidden="true" />
          {isLoading ? 'Шинжилж байна' : 'Профайл үүсгэх'}
        </button>
      </div>
      <label className="select-label">
        Өгөгдөл
        <select
          value={selectedDatasetId ?? ''}
          onChange={(event) => onSelectDataset(event.target.value === '' ? null : Number(event.target.value))}
        >
          <option value="" disabled>Өгөгдөл сонгох</option>
          {datasets.map((dataset) => (
            <option key={dataset.id} value={dataset.id}>
              {dataset.id} - {dataset.name}
            </option>
          ))}
        </select>
      </label>
      {error && <StatusMessage type="error" message={error} />}
      {profile ? <ProfileView profile={profile} /> : <div className="empty-profile">Өгөгдөл сонгоод профайл үүсгэнэ үү.</div>}
    </section>
  );
}
