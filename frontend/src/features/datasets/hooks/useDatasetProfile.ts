import { useCallback, useState } from 'react';

import { fetchDatasetProfile } from '../api/datasetApi';
import type { DatasetProfile } from '../model/datasetTypes';

export function useDatasetProfile() {
  const [profile, setProfile] = useState<DatasetProfile | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(false);

  const loadProfile = useCallback(async (datasetId: number | null) => {
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
  }, []);

  const clearProfile = useCallback(() => {
    setProfile(null);
  }, []);

  const clearProfileError = useCallback(() => {
    setProfileError(null);
  }, []);

  return {
    clearProfile,
    clearProfileError,
    isLoadingProfile,
    loadProfile,
    profile,
    profileError
  };
}
