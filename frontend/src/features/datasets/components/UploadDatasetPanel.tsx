import { UploadCloud } from 'lucide-react';
import { FormEvent, useRef, useState } from 'react';

import { StatusMessage } from './StatusMessage';

type UploadNotice = {
  type: 'success' | 'error';
  message: string;
};

type UploadDatasetPanelProps = {
  isUploading: boolean;
  notice: UploadNotice | null;
  onUpload: (name: string, file: File | null) => Promise<boolean>;
};

export function UploadDatasetPanel({ isUploading, notice, onUpload }: UploadDatasetPanelProps) {
  const [datasetName, setDatasetName] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const uploaded = await onUpload(datasetName, fileInputRef.current?.files?.[0] ?? null);

    if (uploaded) {
      setDatasetName('');
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  }

  return (
    <section className="panel">
      <div className="section-heading">
        <h2>Upload CSV</h2>
      </div>
      <form className="upload-form" onSubmit={handleSubmit}>
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
      {notice && <StatusMessage type={notice.type} message={notice.message} />}
    </section>
  );
}
