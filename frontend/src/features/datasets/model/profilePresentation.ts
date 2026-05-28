import type { ColumnProfile } from './datasetTypes';

export function getColumnWarnings(column: ColumnProfile) {
  const warnings: string[] = [];
  if (column.missingValues > 0) {
    warnings.push(`${formatPercent(column.missingPercentage)} missing`);
  }
  if (column.outlierCount > 0) {
    warnings.push(`${column.outlierCount} outlier${column.outlierCount === 1 ? '' : 's'}`);
  }
  if (column.inferredType === 'MIXED') {
    warnings.push('Mixed values');
  }
  if (column.inferredType === 'EMPTY') {
    warnings.push('Empty');
  }
  if (hasLowDiversity(column)) {
    warnings.push('Low diversity');
  }
  return warnings;
}

export function getColumnSummary(column: ColumnProfile) {
  if (column.inferredType === 'NUMERIC' || column.numericValueCount > 0) {
    return numericSummary(column);
  }
  return textSummary(column);
}

export function getInsightCategory(insight: string) {
  const normalized = insight.toLowerCase();
  if (normalized.includes('outlier')) {
    return 'Possible outlier';
  }
  if (normalized.includes('missing')) {
    return 'Missing values';
  }
  if (normalized.includes('low diversity')) {
    return 'Low diversity';
  }
  if (normalized.includes('wide value range')) {
    return 'Wide range';
  }
  return 'Insight';
}

export function getQualityGradeClass(grade: string) {
  if (grade === 'A') {
    return 'quality-a';
  }
  if (grade === 'B') {
    return 'quality-b';
  }
  if (grade === 'C') {
    return 'quality-c';
  }
  return 'quality-d';
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value));
}

export function formatPercent(value: number) {
  return `${value.toFixed(value % 1 === 0 ? 0 : 1)}%`;
}

function hasLowDiversity(column: ColumnProfile) {
  const presentValues = column.totalValues - column.missingValues;
  return column.inferredType !== 'NUMERIC'
    && column.inferredType !== 'EMPTY'
    && presentValues >= 4
    && column.distinctCount > 0
    && column.distinctCount <= 2;
}

function numericSummary(column: ColumnProfile) {
  if (column.inferredType !== 'NUMERIC' && column.numericValueCount === 0) {
    return '-';
  }

  return [
    `Min ${formatValue(column.min)}`,
    `Max ${formatValue(column.max)}`,
    `Mean ${formatValue(column.mean)}`,
    `Median ${formatValue(column.median)}`,
    `Std ${formatValue(column.standardDeviation)}`
  ].join(', ');
}

function textSummary(column: ColumnProfile) {
  const topValues = Object.entries(column.mostCommonValues ?? {})
    .map(([value, count]) => `${value} (${count})`)
    .join(', ');
  const averageLength = column.averageTextLength == null ? null : `avg length ${formatValue(column.averageTextLength)}`;
  return [topValues, averageLength].filter(Boolean).join('; ') || '-';
}

function formatValue(value: number | null) {
  if (value == null) {
    return '-';
  }
  return Number.isInteger(value) ? String(value) : value.toFixed(2);
}
