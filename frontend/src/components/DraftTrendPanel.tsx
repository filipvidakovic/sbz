import type { DraftTrend, TrendType } from '../types';
import styles from './DraftTrendPanel.module.css';

interface Props {
  trends: DraftTrend[];
}

const TREND_LABELS: Record<TrendType, string> = {
  AGGRESSIVE_ASSASSIN_DRAFT: 'Aggressive Assassin Draft',
  TANK_SPAM:                 'Tank Spam Sequence',
  AP_BURST_TREND:            'AP Burst Trend',
  AD_HEAVY_TREND:            'AD Heavy Trend',
  BURST_DRAFTING:            'Burst Drafting Tempo',
  ENGAGE_CHAIN:              'Engage Chain',
  BALANCED_DRAFT:            'Balanced Draft',
};

const TREND_ICONS: Record<TrendType, string> = {
  AGGRESSIVE_ASSASSIN_DRAFT: '⚔',
  TANK_SPAM:                 '🛡',
  AP_BURST_TREND:            '✦',
  AD_HEAVY_TREND:            '⚡',
  BURST_DRAFTING:            '⚡',
  ENGAGE_CHAIN:              '⬡',
  BALANCED_DRAFT:            '◈',
};

const TREND_SEVERITY: Record<TrendType, 'high' | 'medium' | 'low'> = {
  AGGRESSIVE_ASSASSIN_DRAFT: 'high',
  TANK_SPAM:                 'high',
  AP_BURST_TREND:            'high',
  AD_HEAVY_TREND:            'medium',
  BURST_DRAFTING:            'medium',
  ENGAGE_CHAIN:              'medium',
  BALANCED_DRAFT:            'low',
};

export function DraftTrendPanel({ trends }: Props) {
  if (!trends.length) return null;

  return (
    <div className={styles.panel}>
      <div className={styles.header}>
        <span className={styles.pulse} />
        <span className={styles.title}>CEP DRAFT ANALYSIS</span>
        <span className={styles.badge}>{trends.length} trend{trends.length > 1 ? 's' : ''}</span>
      </div>

      <div className={styles.list}>
        {trends.map((t, i) => (
          <div
            key={i}
            className={`${styles.trend} ${styles[TREND_SEVERITY[t.trendType]]}`}
            style={{ animationDelay: `${i * 60}ms` }}
          >
            <span className={styles.icon}>{TREND_ICONS[t.trendType]}</span>
            <div className={styles.info}>
              <div className={styles.trendName}>{TREND_LABELS[t.trendType]}</div>
              <div className={styles.desc}>{t.description}</div>
            </div>
            <div className={styles.strength}>
              <div className={styles.strengthBar}>
                <div
                  className={styles.strengthFill}
                  style={{ width: `${Math.round(t.strength * 100)}%` }}
                />
              </div>
              <span className={styles.strengthVal}>{Math.round(t.strength * 100)}%</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}