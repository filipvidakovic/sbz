import { useState, useMemo } from 'react';
import { TeamPanel } from './components/TeamPanel';
import { RecommendationCard } from './components/RecommendationCard';
import { useChampions, useRecommendations } from './hooks/useRecommendations';
import styles from './App.module.css';

const SLOTS = 5;

function App() {
  const { champions, loading: champsLoading, error: champsError } = useChampions();
  const { recommendations, loading: recLoading, error: recError, getRecommendations, clearRecommendations } = useRecommendations();

  const [allySlots, setAllySlots] = useState<(string | null)[]>(Array(SLOTS).fill(null));
  const [enemySlots, setEnemySlots] = useState<(string | null)[]>(Array(SLOTS).fill(null));

  const allSelected = useMemo(
    () => [...allySlots, ...enemySlots].filter(Boolean) as string[],
    [allySlots, enemySlots]
  );

  const handleUpdate = (side: 'ally' | 'enemy', index: number, name: string | null) => {
    const setter = side === 'ally' ? setAllySlots : setEnemySlots;
    setter(prev => {
      const next = [...prev];
      next[index] = name;
      return next;
    });
    clearRecommendations();
  };

  const handleAnalyze = () => {
    const ally = allySlots.filter(Boolean) as string[];
    const enemy = enemySlots.filter(Boolean) as string[];
    getRecommendations(ally, enemy);
  };

  const handleReset = () => {
    setAllySlots(Array(SLOTS).fill(null));
    setEnemySlots(Array(SLOTS).fill(null));
    clearRecommendations();
  };

  const hasAny = allSelected.length > 0;
  const canAnalyze = (allySlots.some(Boolean) || enemySlots.some(Boolean)) && !recLoading;

  return (
    <div className={styles.app}>
      {/* Background grid pattern */}
      <div className={styles.grid} aria-hidden />

      <header className={styles.header}>
        <div className={styles.logo}>
          <span className={styles.logoIcon}>⬡</span>
          <div>
            <h1 className={styles.title}>CHAMPION ADVISOR</h1>
            <p className={styles.subtitle}>Rule-Based Pick Recommendation</p>
          </div>
        </div>
      </header>

      <main className={styles.main}>
        {champsError && (
          <div className={styles.errorBanner}>
            ⚠ {champsError}
          </div>
        )}

        <div className={styles.layout}>
          {/* Left: Teams */}
          <section className={styles.teams}>
            {champsLoading ? (
              <div className={styles.loading}>
                <span className={styles.spinner} />
                Connecting to backend...
              </div>
            ) : (
              <>
                <TeamPanel
                  side="ally"
                  slots={allySlots}
                  champions={champions}
                  allSelected={allSelected}
                  onUpdate={(i, n) => handleUpdate('ally', i, n)}
                />

                <div className={styles.vs}>VS</div>

                <TeamPanel
                  side="enemy"
                  slots={enemySlots}
                  champions={champions}
                  allSelected={allSelected}
                  onUpdate={(i, n) => handleUpdate('enemy', i, n)}
                />
              </>
            )}

            <div className={styles.actions}>
              <button
                className={styles.analyzeBtn}
                onClick={handleAnalyze}
                disabled={!canAnalyze}
              >
                {recLoading ? (
                  <><span className={styles.spinnerSm} /> ANALYZING...</>
                ) : (
                  '⬡ ANALYZE MATCHUP'
                )}
              </button>

              {hasAny && (
                <button className={styles.resetBtn} onClick={handleReset}>
                  RESET
                </button>
              )}
            </div>
          </section>

          {/* Right: Recommendations */}
          <section className={styles.results}>
            <div className={styles.resultsHeader}>
              <h2 className={styles.resultsTitle}>RECOMMENDATIONS</h2>
              {recommendations.length > 0 && (
                <span className={styles.resultCount}>{recommendations.length} found</span>
              )}
            </div>

            {recError && (
              <div className={styles.errorBanner}>⚠ {recError}</div>
            )}

            {recommendations.length === 0 && !recLoading && (
              <div className={styles.empty}>
                <div className={styles.emptyIcon}>⬡</div>
                <p>Select champions and click<br />Analyze Matchup</p>
              </div>
            )}

            <div className={styles.recList}>
              {recommendations.map((rec, i) => (
                <RecommendationCard key={rec.championName} rec={rec} rank={i} />
              ))}
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;