import { AlertCircle, CheckCircle2 } from 'lucide-react';

type StatusMessageProps = {
  type: 'success' | 'error';
  message: string;
};

export function StatusMessage({ type, message }: StatusMessageProps) {
  const Icon = type === 'success' ? CheckCircle2 : AlertCircle;
  return (
    <div className={`status-message ${type}`}>
      <Icon size={18} aria-hidden="true" />
      <span>{message}</span>
    </div>
  );
}
