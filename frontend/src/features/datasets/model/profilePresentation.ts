import type { ColumnProfile } from './datasetTypes';

type ColumnType = ColumnProfile['inferredType'];

export function getColumnWarnings(column: ColumnProfile) {
  const warnings: string[] = [];
  if (column.missingValues > 0) {
    warnings.push(`${formatPercent(column.missingPercentage)} хоосон`);
  }
  if (column.outlierCount > 0) {
    warnings.push(`${column.outlierCount} хэт ялгаатай утга`);
  }
  if (column.inferredType === 'MIXED') {
    warnings.push('Холимог утгууд');
  }
  if (column.inferredType === 'EMPTY') {
    warnings.push('Хоосон');
  }
  if (hasLowDiversity(column)) {
    warnings.push('Бага төрөлжилт');
  }
  return warnings;
}

export function getColumnSummary(column: ColumnProfile) {
  if (column.inferredType === 'NUMERIC' || column.numericValueCount > 0) {
    return numericSummary(column);
  }
  return textSummary(column);
}

export function getColumnTypeLabel(type: ColumnType) {
  if (type === 'NUMERIC') {
    return 'Тоон';
  }
  if (type === 'TEXT') {
    return 'Текст';
  }
  if (type === 'MIXED') {
    return 'Холимог';
  }
  return 'Хоосон';
}

export function getInsightCategory(insight: string) {
  const normalized = insight.toLowerCase();
  if (normalized.includes('outlier')) {
    return 'Хэт ялгаатай байж болзошгүй';
  }
  if (normalized.includes('missing')) {
    return 'Хоосон утгууд';
  }
  if (normalized.includes('low diversity')) {
    return 'Бага төрөлжилт';
  }
  if (normalized.includes('wide value range')) {
    return 'Өргөн хэлбэлзэл';
  }
  return 'Дүгнэлт';
}

export function getDisplayInsight(insight: string) {
  const noIssues = 'No major data quality or distribution issues were detected by the current rules.';
  if (insight === noIssues) {
    return 'Одоогийн дүрмээр өгөгдлийн чанар эсвэл тархалтын томоохон асуудал илрээгүй.';
  }

  const missing = insight.match(/^Column '(.+)' has ([\d.]+%) missing values\.$/);
  if (missing) {
    return `Багана "${missing[1]}" ${missing[2]} хоосон утгатай байна.`;
  }

  const outliers = insight.match(/^Column '(.+)' contains (\d+) possible outliers\.$/);
  if (outliers) {
    return `Багана "${outliers[1]}" ${outliers[2]} хэт ялгаатай байж болзошгүй утгатай байна.`;
  }

  const lowDiversity = insight.match(/^Column '(.+)' has low diversity: only (\d+) distinct values\.$/);
  if (lowDiversity) {
    return `Багана "${lowDiversity[1]}" бага төрөлжилттэй: зөвхөн ${lowDiversity[2]} ялгаатай утгатай.`;
  }

  const wideRange = insight.match(/^Column '(.+)' appears to be numeric with a wide value range\.$/);
  if (wideRange) {
    return `Багана "${wideRange[1]}" тоон төрөлтэй бөгөөд утгын хэлбэлзэл өргөн байна.`;
  }

  const mixed = insight.match(/^Column '(.+)' mixes numeric and text values\.$/);
  if (mixed) {
    return `Багана "${mixed[1]}" тоон болон текст утгыг хольсон байна.`;
  }

  const empty = insight.match(/^Column '(.+)' is empty\.$/);
  if (empty) {
    return `Багана "${empty[1]}" хоосон байна.`;
  }

  return insight;
}

export function getQualityExplanation(explanation: string) {
  if (explanation === 'Dataset has no row values to evaluate.') {
    return 'Үнэлэх мөрийн утга алга.';
  }
  if (explanation === 'Dataset is complete and consistent based on the current checks.') {
    return 'Одоогийн шалгалтаар өгөгдөл бүрэн бөгөөд тогтвортой байна.';
  }

  const mostlyComplete = explanation.match(/^Dataset is mostly complete but contains (.+)\.$/);
  if (mostlyComplete) {
    return `Өгөгдөл ихэнхдээ бүрэн боловч ${translateIssueText(mostlyComplete[1])} агуулж байна.`;
  }

  const needsReview = explanation.match(/^Dataset needs review because it contains (.+)\.$/);
  if (needsReview) {
    return `Өгөгдөл ${translateIssueText(needsReview[1])} агуулж байгаа тул шалгах шаардлагатай.`;
  }

  const significantIssues = explanation.match(/^Dataset has significant quality issues including (.+)\.$/);
  if (significantIssues) {
    return `Өгөгдөлд ${translateIssueText(significantIssues[1])} зэрэг ноцтой чанарын асуудал байна.`;
  }

  return explanation;
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
  return new Intl.DateTimeFormat('mn-MN', {
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
    `Бага ${formatValue(column.min)}`,
    `Их ${formatValue(column.max)}`,
    `Дундаж ${formatValue(column.mean)}`,
    `Медиан ${formatValue(column.median)}`,
    `Ст. хазайлт ${formatValue(column.standardDeviation)}`
  ].join(', ');
}

function textSummary(column: ColumnProfile) {
  const topValues = Object.entries(column.mostCommonValues ?? {})
    .map(([value, count]) => `${value} (${count})`)
    .join(', ');
  const averageLength = column.averageTextLength == null ? null : `дундаж урт ${formatValue(column.averageTextLength)}`;
  return [topValues, averageLength].filter(Boolean).join('; ') || '-';
}

function translateIssueText(issueText: string) {
  const issues = issueText.split(/, | and /).map((issue) => {
    if (issue === 'missing values') {
      return 'хоосон утга';
    }
    if (issue === 'mixed-type columns') {
      return 'холимог төрөлтэй багана';
    }
    if (issue === 'possible outliers') {
      return 'хэт ялгаатай байж болзошгүй утга';
    }
    if (issue === 'empty columns') {
      return 'хоосон багана';
    }
    if (issue === 'repeated quality warnings') {
      return 'давтагдсан чанарын анхааруулга';
    }
    return issue;
  });

  if (issues.length <= 1) {
    return issues[0] ?? issueText;
  }

  return `${issues.slice(0, -1).join(', ')} болон ${issues[issues.length - 1]}`;
}

function formatValue(value: number | null) {
  if (value == null) {
    return '-';
  }
  return Number.isInteger(value) ? String(value) : value.toFixed(2);
}
