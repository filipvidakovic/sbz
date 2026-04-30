import { ChampionPicker } from './ChampionPicker';
import styles from './TeamPanel.module.css';
import type { TeamSide } from '../types';

interface Props {
  side: TeamSide;
  slots: (string | null)[];
  champions: string[];
  allSelected: string[];
  onUpdate: (index: number, name: string | null) => void;
}

const LABELS: Record<TeamSide, string> = {
  ally: 'YOUR TEAM',
  enemy: 'ENEMY TEAM',
};

const SLOT_ROLES = ['Top', 'Jungle', 'Mid', 'Bot', 'Support'];

export function TeamPanel({ side, slots, champions, allSelected, onUpdate }: Props) {
  return (
    <div className={`${styles.panel} ${styles[side]}`}>
      <div className={styles.header}>
        <div className={styles.indicator} />
        <h2 className={styles.title}>{LABELS[side]}</h2>
        <span className={styles.count}>
          {slots.filter(Boolean).length}/5
        </span>
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
    </div>
  );
}