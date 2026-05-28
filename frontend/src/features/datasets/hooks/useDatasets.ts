import { useCallback, useEffect, useState } from 'react';

import { fetchDatasets, uploadDataset } from '../api/datasetApi';
import type { DatasetSummary } from '../model/datasetTypes';

type Notice = {
  type: 'success' | 'error';
  message: string;
};

export function useDatasets() {
  const [datasets, setDatasets] = useState<DatasetSummary[]>([]);
  const [selectedDatasetId, setSelectedDatasetId] = useState<number | null>(null);
  const [uploadNotice, setUploadNotice] = useState<Notice | null>(null);
  const [listError, setListError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isLoadingDatasets, setIsLoadingDatasets] = useState(false);

  const loadDatasets = useCallback(async (nextSelectedId?: number) => {
    setIsLoadingDatasets(true);
    setListError(null);
    try {
      const result = await fetchDatasets();
      setDatasets(result);
      setSelectedDatasetId((currentSelectedId) => {
        if (nextSelectedId != null) {
          return nextSelectedId;
        }
        if (currentSelectedId != null && result.some((dataset) => dataset.id === currentSelectedId)) {
          return currentSelectedId;
        }
        return result[0]?.id ?? null;
      });
    } catch (error) {
      setListError(readErrorMessage(error, 'Failed to load datasets.'));
    } finally {
      setIsLoadingDatasets(false);
    }
  }, []);

  useEffect(() => {
    void loadDatasets();
  }, [loadDatasets]);

  const uploadDatasetFile = useCallback(async (name: string, file: File | null) => {
    const trimmedName = name.trim();
    if (!trimmedName || !file) {
      setUploadNotice({ type: 'error', message: 'Dataset name and CSV file are required.' });
      return null;
    }

    setIsUploading(true);
    setUploadNotice(null);
    try {
      const uploaded = await uploadDataset(trimmedName, file);
      setUploadNotice({
        type: 'success',
        message: `Dataset uploaded successfully. "${uploaded.name}" is ready for analysis.`
      });
      await loadDatasets(uploaded.id);
      return uploaded;
    } catch (error) {
      setUploadNotice({ type: 'error', message: readErrorMessage(error, 'Upload failed.') });
      return null;
    } finally {
      setIsUploading(false);
    }
  }, [loadDatasets]);

  const selectDataset = useCallback((datasetId: number | null) => {
    setSelectedDatasetId(datasetId);
  }, []);

  return {
    datasets,
    isLoadingDatasets,
    isUploading,
    listError,
    selectedDatasetId,
    selectDataset,
    uploadDatasetFile,
    uploadNotice,
    refreshDatasets: loadDatasets
  };
}

function readErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
