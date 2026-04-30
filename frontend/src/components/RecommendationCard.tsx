import { useState } from 'react';
import styles from './RecommendationCard.module.css';
import type { Recommendation } from '../types';

interface Props {
  rec: Recommendation;
  rank: number;
}

export function RecommendationCard({ rec, rank }: Props) {
  const [expanded, setExpanded] = useState(rank === 0);

  return (
    <div
      className={`${styles.card} ${rank === 0 ? styles.top : ''}`}
      style={{ animationDelay: `${rank * 80}ms` }}
    >
      <div className={styles.header} onClick={() => setExpanded(e => !e)}>
        <span className={styles.rank}>#{rank + 1}</span>

        <div className={styles.name}>{rec.championName}</div>

        <div className={styles.priority}>
          <div className={styles.bar}>
            <div
              className={styles.fill}
              style={{ width: `${rec.priority}%` }}
            />
          </div>
          <span className={styles.score}>{rec.priority}</span>
        </div>

        <span className={styles.toggle}>{expanded ? '▲' : '▼'}</span>
      </div>

      {expanded && (
        <ul className={styles.reasons}>
          {rec.reasons.map((r, i) => (
            <li key={i} className={styles.reason}>
              <span className={styles.dot}>◆</span>
              {r}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}