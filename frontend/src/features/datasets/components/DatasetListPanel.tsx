import { RefreshCw } from 'lucide-react';

import type { DatasetSummary } from '../model/datasetTypes';
import { formatDate } from '../model/profilePresentation';
import { StatusMessage } from './StatusMessage';

type DatasetListPanelProps = {
  datasets: DatasetSummary[];
  error: string | null;
  isLoading: boolean;
  selectedDatasetId: number | null;
  onRefresh: () => Promise<void>;
  onSelectDataset: (datasetId: number) => void;
};

export function DatasetListPanel({
  datasets,
  error,
  isLoading,
  selectedDatasetId,
  onRefresh,
  onSelectDataset
}: DatasetListPanelProps) {
  return (
    <section className="panel dataset-panel">
      <div className="section-heading with-action">
        <h2>Өгөгдлүүд</h2>
        <button className="icon-button" type="button" onClick={() => void onRefresh()} disabled={isLoading}>
          <RefreshCw size={17} aria-hidden="true" />
          Шинэчлэх
        </button>
      </div>
      {error && <StatusMessage type="error" message={error} />}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Нэр</th>
              <th>Мөр</th>
              <th>Багана</th>
              <th>Үүсгэсэн</th>
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
                    onClick={() => onSelectDataset(dataset.id)}
                    disabled={selectedDatasetId === dataset.id}
                  >
                    {selectedDatasetId === dataset.id ? 'Сонгосон' : 'Сонгох'}
                  </button>
                </td>
              </tr>
            ))}
            {!isLoading && datasets.length === 0 && (
              <tr>
                <td colSpan={6} className="empty-cell">Одоогоор өгөгдөл алга.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
