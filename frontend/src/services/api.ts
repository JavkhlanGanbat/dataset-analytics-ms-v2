import { API_GATEWAY_URL } from './config';

export type DatasetSummary = {
  id: number;
  name: string;
  columnCount: number;
  rowCount: number;
  createdAt: string;
};

export type ColumnProfile = {
  columnName: string;
  inferredType: 'NUMERIC' | 'TEXT' | 'MIXED' | 'EMPTY';
  totalValues: number;
  missingValues: number;
  missingPercentage: number;
  distinctCount: number;
  numericValueCount: number;
  min: number | null;
  max: number | null;
  mean: number | null;
  median: number | null;
  standardDeviation: number | null;
  outlierCount: number;
  mostCommonValues: Record<string, number>;
  averageTextLength: number | null;
};

export type DataQualityScore = {
  score: number;
  grade: string;
  explanation: string;
};

export type DatasetProfile = {
  datasetId: number;
  datasetName: string;
  totalRowCount: number;
  totalColumnCount: number;
  columns: ColumnProfile[];
  dataQuality: DataQualityScore;
  insights: string[];
};

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

async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_GATEWAY_URL}${path}`, init);
  if (!response.ok) {
    const message = await readErrorMessage(response);
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

async function readErrorMessage(response: Response): Promise<string> {
  try {
    const text = await response.text();
    return text || response.statusText;
  } catch {
    return response.statusText;
  }
}
