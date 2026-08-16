import React from 'react';

function formatMoney(value) {
  if (value === null || value === undefined) return '-';
  return Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

/**
 * Renders simple rule warnings from /rules/status:
 * - persistent banner when the daily loss limit has been hit
 * - progress bar toward the monthly P&L goal (when set)
 * Non-blocking — purely informational.
 */
export default function RulesWidget({ status }) {
  if (!status) return null;

  const { dailyLimitHit, monthlyPnl, monthlyGoalPnl, monthlyGoalProgressPercent } = status;
  const hasGoal = monthlyGoalPnl !== null && monthlyGoalPnl !== undefined && Number(monthlyGoalPnl) !== 0;

  const progress = hasGoal
    ? Math.max(0, Math.min(100, Number(monthlyGoalProgressPercent ?? 0)))
    : 0;

  return (
    <>
      {dailyLimitHit && (
        <div className="rule-banner" role="alert">
          ⚠️ Daily loss limit reached — consider stepping away
        </div>
      )}

      {hasGoal && (
        <div className="rule-goal-card">
          <div className="rule-goal-header">
            <span>Monthly goal progress</span>
            <span>
              <strong>{formatMoney(monthlyPnl)}</strong> / {formatMoney(monthlyGoalPnl)}
            </span>
          </div>
          <div className="rule-progress-track">
            <div
              className="rule-progress-fill"
              style={{ width: `${progress}%` }}
              title={`${Number(monthlyGoalProgressPercent ?? 0).toFixed(1)}% of monthly goal`}
            />
          </div>
        </div>
      )}
    </>
  );
}
