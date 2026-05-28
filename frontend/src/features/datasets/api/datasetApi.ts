import { apiRequest } from '../../../shared/api/httpClient';
import type { DatasetProfile, DatasetSummary } from '../model/datasetTypes';

export async function fetchDatasets(): Promise<DatasetSummary[]> {
  return apiRequest('/datasets');
}

export async function uploadDataset(name: string, file: File): Promise<DatasetSummary> {
  const formData = new FormData();
  formData.append('name', name);
  formData.append('file', file);

  return apiRequest('/datasets/upload', {
    method: 'POST',
    body: formData
  });
}

export async function fetchDatasetProfile(datasetId: number): Promise<DatasetProfile> {
  return apiRequest(`/analytics/datasets/${datasetId}/profile`);
}
