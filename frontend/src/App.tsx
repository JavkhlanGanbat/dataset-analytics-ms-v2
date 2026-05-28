import { DatasetListPanel } from './features/datasets/components/DatasetListPanel';
import { DatasetProfilePanel } from './features/datasets/components/DatasetProfilePanel';
import { UploadDatasetPanel } from './features/datasets/components/UploadDatasetPanel';
import { useDatasetProfile } from './features/datasets/hooks/useDatasetProfile';
import { useDatasets } from './features/datasets/hooks/useDatasets';

function App() {
  const datasetState = useDatasets();
  const profileState = useDatasetProfile();

  async function handleUpload(name: string, file: File | null) {
    const uploaded = await datasetState.uploadDatasetFile(name, file);
    if (uploaded) {
      profileState.clearProfile();
    }
    return uploaded != null;
  }

  function handleDatasetSelection(datasetId: number | null) {
    datasetState.selectDataset(datasetId);
    profileState.clearProfile();
    profileState.clearProfileError();
  }

  async function handleGenerateProfile() {
    await profileState.loadProfile(datasetState.selectedDatasetId);
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <div>
          <h1>Өгөгдөл шинжлэгч</h1>
          <p className="subtitle">
            CSV файл оруулаад хоосон утга, хэт ялгаатай утга, чанарын асуудлыг шууд илрүүлээрэй.
          </p>
        </div>
      </header>

      <div className="workspace-grid">
        <UploadDatasetPanel
          isUploading={datasetState.isUploading}
          notice={datasetState.uploadNotice}
          onUpload={handleUpload}
        />

        <DatasetListPanel
          datasets={datasetState.datasets}
          error={datasetState.listError}
          isLoading={datasetState.isLoadingDatasets}
          selectedDatasetId={datasetState.selectedDatasetId}
          onRefresh={datasetState.refreshDatasets}
          onSelectDataset={handleDatasetSelection}
        />

        <DatasetProfilePanel
          datasets={datasetState.datasets}
          error={profileState.profileError}
          isLoading={profileState.isLoadingProfile}
          profile={profileState.profile}
          selectedDatasetId={datasetState.selectedDatasetId}
          onGenerateProfile={handleGenerateProfile}
          onSelectDataset={handleDatasetSelection}
        />
      </div>
    </main>
  );
}

export default App;
