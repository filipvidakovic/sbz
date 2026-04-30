import { useState, useRef, useEffect } from 'react';
import styles from './ChampionPicker.module.css';

interface Props {
  champions: string[];
  selected: string | null;
  onSelect: (name: string | null) => void;
  placeholder?: string;
  excluded?: string[];
}

export function ChampionPicker({ champions, selected, onSelect, placeholder = 'Pick champion...', excluded = [] }: Props) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const ref = useRef<HTMLDivElement>(null);

  const available = champions.filter(
    c => !excluded.includes(c) && c.toLowerCase().includes(search.toLowerCase())
  );

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
        setSearch('');
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div className={styles.wrapper} ref={ref}>
      <button
        className={`${styles.trigger} ${selected ? styles.filled : ''}`}
        onClick={() => setOpen(o => !o)}
      >
        {selected ? (
          <>
            <span className={styles.championName}>{selected}</span>
            <span
              className={styles.clear}
              onClick={e => { e.stopPropagation(); onSelect(null); }}
            >✕</span>
          </>
        ) : (
          <span className={styles.placeholder}>{placeholder}</span>
        )}
        <span className={styles.arrow}>{open ? '▲' : '▼'}</span>
      </button>

      {open && (
        <div className={styles.dropdown}>
          <input
            className={styles.search}
            placeholder="Search..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            autoFocus
          />
          <div className={styles.list}>
            {available.length === 0 ? (
              <div className={styles.empty}>No champions found</div>
            ) : (
              available.map(c => (
                <button
                  key={c}
                  className={`${styles.item} ${c === selected ? styles.active : ''}`}
                  onClick={() => { onSelect(c); setOpen(false); setSearch(''); }}
                >
                  {c}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}