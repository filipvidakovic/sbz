import { useState } from 'react';
import { ChampionPicker } from './ChampionPicker';
import { useChampionCheck } from '../hooks/useRecommendations';
import type { RecommendationRequest } from '../types';
import styles from './ChampionChecker.module.css';

interface Props {
  champions: string[];
  context: RecommendationRequest;
}

export function ChampionChecker({ champions, context }: Props) {
  const [selected, setSelected] = useState<string | null>(null);
  const { result, loading, error, check, clear } = useChampionCheck();

  const handleCheck = () => {
    if (selected) check(selected, context);
  };

  const handleSelect = (name: string | null) => {
    setSelected(name);
    clear();
  };

  return (
    <div className={styles.panel}>
      <div className={styles.header}>
        <span className={styles.icon}>◈</span>
        <span className={styles.title}>BACKWARD CHAIN CHECK</span>
      </div>

      <div className={styles.body}>
        <p className={styles.hint}>Ask the engine: is this a good pick?</p>

        <div className={styles.row}>
          <div className={styles.pickerWrap}>
            <ChampionPicker
              champions={champions}
              selected={selected}
              onSelect={handleSelect}
              placeholder="Select champion to check..."
            />
          </div>
          <button
            className={styles.checkBtn}
            onClick={handleCheck}
            disabled={!selected || loading}
          >
            {loading ? <span className={styles.spinner} /> : 'CHECK'}
          </button>
        </div>

        {error && <div className={styles.error}>⚠ {error}</div>}

        {result && (
          <div className={`${styles.result} ${result.valid ? styles.valid : styles.invalid}`}>
            <div className={styles.verdict}>
              <span className={styles.verdictIcon}>{result.valid ? '✓' : '✗'}</span>
              <span className={styles.verdictName}>{result.championName}</span>
              <span className={styles.verdictLabel}>
                {result.valid ? 'RECOMMENDED' : 'NOT RECOMMENDED'}
              </span>
            </div>
            <ul className={styles.reasons}>
              {result.reasons.map((r, i) => (
                <li key={i} className={styles.reason}>
                  <span className={styles.dot}>▸</span>
                  {r}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}