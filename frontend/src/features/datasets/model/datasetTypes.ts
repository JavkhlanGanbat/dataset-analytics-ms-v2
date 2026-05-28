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
