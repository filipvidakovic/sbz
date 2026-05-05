import { useState, useMemo } from 'react';
import { TeamPanel } from './components/TeamPanel';
import { RecommendationCard } from './components/RecommendationCard';
import { DraftTrendPanel } from './components/DraftTrendPanel';
import { ChampionChecker } from './components/ChampionChecker';
import { useChampions, useRecommendations } from './hooks/useRecommendations';
import styles from './App.module.css';

const SLOTS = 5;

function App() {
  const { champions, loading: champsLoading, error: champsError } = useChampions();
  const { recommendations, trends, cepActive, loading: recLoading, error: recError, getRecommendations, clear } = useRecommendations();

  const [allySlots, setAllySlots] = useState<(string | null)[]>(Array(SLOTS).fill(null));
  const [enemySlots, setEnemySlots] = useState<(string | null)[]>(Array(SLOTS).fill(null));
  const [cepEnabled, setCepEnabled] = useState(false);
  const [pickInterval, setPickInterval] = useState(8000);

  const allSelected = useMemo(
    () => [...allySlots, ...enemySlots].filter(Boolean) as string[],
    [allySlots, enemySlots]
  );

  // Kontekst za BC checker
  const currentContext = useMemo(() => ({
    allyChampions: allySlots.filter(Boolean) as string[],
    enemyChampions: enemySlots.filter(Boolean) as string[],
  }), [allySlots, enemySlots]);

  const handleUpdate = (side: 'ally' | 'enemy', index: number, name: string | null) => {
    const setter = side === 'ally' ? setAllySlots : setEnemySlots;
    setter(prev => { const n = [...prev]; n[index] = name; return n; });
    clear();
  };

  const handleAnalyze = () => {
    const ally = allySlots.filter(Boolean) as string[];
    const enemy = enemySlots.filter(Boolean) as string[];
    const orderedEnemyPicks = cepEnabled
      ? enemySlots.filter(Boolean) as string[]
      : undefined;

    getRecommendations({
      allyChampions: ally,
      enemyChampions: enemy,
      orderedEnemyPicks,
      simulatedPickIntervalMs: cepEnabled ? pickInterval : undefined,
    });
  };

  const handleReset = () => {
    setAllySlots(Array(SLOTS).fill(null));
    setEnemySlots(Array(SLOTS).fill(null));
    clear();
  };

  const hasAny = allSelected.length > 0;
  const canAnalyze = hasAny && !recLoading;

  return (
    <div className={styles.app}>
      <div className={styles.grid} aria-hidden />

      <header className={styles.header}>
        <div className={styles.logo}>
          <span className={styles.logoIcon}>⬡</span>
          <div>
            <h1 className={styles.title}>CHAMPION ADVISOR</h1>
            <p className={styles.subtitle}>Drools Rule-Based Pick Recommendation</p>
          </div>
        </div>
        {cepActive && (
          <div className={styles.cepBadge}>
            <span className={styles.cepDot} />
            CEP ACTIVE
          </div>
        )}
      </header>

      <main className={styles.main}>
        {champsError && <div className={styles.errorBanner}>⚠ {champsError}</div>}

        <div className={styles.layout}>

          {/* ── Leva kolona: timovi ── */}
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
                  cepEnabled={cepEnabled}
                  onCepToggle={setCepEnabled}
                  pickInterval={pickInterval}
                  onPickIntervalChange={setPickInterval}
                />
              </>
            )}

            <div className={styles.actions}>
              <button
                className={styles.analyzeBtn}
                onClick={handleAnalyze}
                disabled={!canAnalyze}
              >
                {recLoading
                  ? <><span className={styles.spinnerSm} /> ANALYZING...</>
                  : '⬡ ANALYZE MATCHUP'
                }
              </button>
              {hasAny && (
                <button className={styles.resetBtn} onClick={handleReset}>RESET</button>
              )}
            </div>
          </section>

          {/* ── Desna kolona: rezultati ── */}
          <section className={styles.results}>

            {/* CEP trendovi */}
            {trends.length > 0 && <DraftTrendPanel trends={trends} />}

            {/* BC checker */}
            {!champsLoading && (
              <ChampionChecker champions={champions} context={currentContext} />
            )}

            {/* Preporuke */}
            <div className={styles.recSection}>
              <div className={styles.resultsHeader}>
                <h2 className={styles.resultsTitle}>RECOMMENDATIONS</h2>
                {recommendations.length > 0 && (
                  <span className={styles.resultCount}>{recommendations.length} found</span>
                )}
              </div>

              {recError && <div className={styles.errorBanner}>⚠ {recError}</div>}

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
            </div>
          </section>

        </div>
      </main>
    </div>
  );
}

export default App;