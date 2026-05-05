import { ChampionPicker } from './ChampionPicker';
import styles from './TeamPanel.module.css';
import type { TeamSide } from '../types';

interface Props {
  side: TeamSide;
  slots: (string | null)[];
  champions: string[];
  allSelected: string[];
  onUpdate: (index: number, name: string | null) => void;
  // CEP: samo za enemy panel
  cepEnabled?: boolean;
  onCepToggle?: (enabled: boolean) => void;
  pickInterval?: number;
  onPickIntervalChange?: (ms: number) => void;
}

const LABELS: Record<TeamSide, string> = {
  ally: 'YOUR TEAM',
  enemy: 'ENEMY TEAM',
};

const SLOT_ROLES = ['Top', 'Jungle', 'Mid', 'Bot', 'Support'];

export function TeamPanel({
  side, slots, champions, allSelected, onUpdate,
  cepEnabled, onCepToggle, pickInterval, onPickIntervalChange
}: Props) {
  return (
    <div className={`${styles.panel} ${styles[side]}`}>
      <div className={styles.header}>
        <div className={styles.indicator} />
        <h2 className={styles.title}>{LABELS[side]}</h2>
        <span className={styles.count}>{slots.filter(Boolean).length}/5</span>
      </div>

      <div className={styles.slots}>
        {slots.map((slot, i) => (
          <div key={i} className={styles.slot}>
            <span className={styles.role}>{SLOT_ROLES[i]}</span>
            <ChampionPicker
              champions={champions}
              selected={slot}
              onSelect={name => onUpdate(i, name)}
              placeholder={`Pick ${SLOT_ROLES[i]}...`}
              excluded={allSelected.filter(s => s !== slot)}
            />
          </div>
        ))}
      </div>

      {/* CEP opcije – samo za enemy panel */}
      {side === 'enemy' && onCepToggle && (
        <div className={styles.cepSection}>
          <label className={styles.cepToggle}>
            <input
              type="checkbox"
              checked={cepEnabled}
              onChange={e => onCepToggle(e.target.checked)}
              className={styles.checkbox}
            />
            <span className={styles.cepLabel}>
              <span className={styles.cepIcon}>⚡</span>
              CEP Draft Analysis
            </span>
          </label>

          {cepEnabled && (
            <div className={styles.cepOptions}>
              <span className={styles.cepHint}>
                Enemy picks will be sent in slot order (Top→Support)
              </span>
              <label className={styles.intervalLabel}>
                Pick interval
                <select
                  className={styles.intervalSelect}
                  value={pickInterval}
                  onChange={e => onPickIntervalChange?.(Number(e.target.value))}
                >
                  <option value={4000}>Fast (4s)</option>
                  <option value={8000}>Normal (8s)</option>
                  <option value={15000}>Slow (15s)</option>
                </select>
              </label>
            </div>
          )}
        </div>
      )}
    </div>
  );
}