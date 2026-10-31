import {
  AdjustRestTimerCommand,
  AppConfiguration,
  CompleteSetCommand,
  SkipRestTimerCommand,
  Translations,
  WorkoutMessage,
} from '@/models/workout-worker-messages';
import WorkoutWorkerModule from '~/modules/workout-worker/src/WorkoutWorkerModule';
import { RootState } from '@/store';
import { selectActiveSessionId, sessionFinished, updateStoredSession } from '@/store/stored-sessions';
import { RecordedCardioExercise, RecordedWeightedExercise, RestTimer } from '@/models/session-models';
import { Weight } from '@/models/weight';
import { Duration, OffsetDateTime } from '@js-joda/core';
import { Dispatch } from '@reduxjs/toolkit';
import { TolgeeInstance, TranslationKey } from '@tolgee/react';

export class WorkoutWorker {
  private readonly listeners = new Map<WorkoutMessage['payload']['type'], ((e: WorkoutMessage['payload']) => void)[]>();

  constructor(
    private dispatch: Dispatch,
    private getState: () => RootState,
    private tolgee: TolgeeInstance,
  ) {
    WorkoutWorkerModule.addListener('on', (encodedEvent) => {
      const decoded = JSON.parse(encodedEvent.jsonString) as WorkoutMessage;
      const listeners = this.listeners.get(decoded.payload.type);
      listeners?.forEach((handler) => handler(decoded.payload));
    });

    this.on('FinishWorkoutCommand', () => this.handleFinishWorkout());
    this.on<CompleteSetCommand>('CompleteSetCommand', (e) => this.handleCompleteSet(e));
    this.on<SkipRestTimerCommand>('SkipRestTimerCommand', () => this.handleSkipRestTimer());
    this.on<AdjustRestTimerCommand>('AdjustRestTimerCommand', (e) => this.handleAdjustRestTimer(e));
  }

  broadcast(event: WorkoutMessage['payload']) {
    WorkoutWorkerModule.broadcast(
      JSON.stringify({
        payload: event,
        appConfiguration: this.getAppConfigurationMessage(),
        translations: this.getTranslationMessage(),
      } satisfies WorkoutMessage),
    );
  }

  private getAppConfigurationMessage(): AppConfiguration {
    return {
      notificationsEnabled: this.getState().settings.restNotifications,
    };
  }

  private handleFinishWorkout() {
    const activeSessionId = selectActiveSessionId(this.getState());
    if (activeSessionId) {
      this.dispatch(sessionFinished(activeSessionId));
    }
  }

  private handleCompleteSet(command: CompleteSetCommand) {
    const activeSessionId = selectActiveSessionId(this.getState());
    if (!activeSessionId) {
      return;
    }

    this.dispatch(
      updateStoredSession({
        sessionId: activeSessionId,
        update: (s) => {
          const ex = s.recordedExercises[command.exerciseIndex];
          if (!ex) {
            return s;
          }

          const now = OffsetDateTime.now();
          if (ex instanceof RecordedWeightedExercise) {
            let updatedEx = ex;
            if (command.weight !== undefined) {
              const weightVal = Weight.fromJSON(command.weight);
              updatedEx = updatedEx.withWeight(command.setIndex, weightVal, 'thisSet');
            }
            const reps = command.reps ?? updatedEx.repsTargetForSet(command.setIndex).max;
            updatedEx = updatedEx.withRepCount(command.setIndex, reps, now);
            const updatedSession = s.withExercise(command.exerciseIndex, updatedEx);
            return updatedSession.with({ restTimer: new RestTimer(updatedSession.lastExercise?.latestTime ?? now) });
          } else if (ex instanceof RecordedCardioExercise) {
            const before = s.cardioSetAt(command.exerciseIndex, command.setIndex);
            const updated = s.withCardioSet(
              command.exerciseIndex,
              command.setIndex,
              (set) => set.with({ completionDateTime: now }),
              now,
            );
            return updated.cardioSetAt(command.exerciseIndex, command.setIndex)?.earnsRest(before)
              ? updated.with({ restTimer: new RestTimer(updated.lastExercise?.latestTime ?? now) })
              : updated;
          }
          return s;
        },
      }),
    );
  }

  private handleSkipRestTimer() {
    const activeSessionId = selectActiveSessionId(this.getState());
    if (!activeSessionId) {
      return;
    }

    this.dispatch(
      updateStoredSession({
        sessionId: activeSessionId,
        update: (s) => s.with({ restTimer: undefined }),
      }),
    );
  }

  private handleAdjustRestTimer(command: AdjustRestTimerCommand) {
    const activeSessionId = selectActiveSessionId(this.getState());
    if (!activeSessionId) {
      return;
    }

    this.dispatch(
      updateStoredSession({
        sessionId: activeSessionId,
        update: (s) => {
          if (!s.restTimer) {
            return s;
          }
          // Adding deltaSeconds adds to remaining rest, so startedAt moves back
          const newStartedAt = s.restTimer.startedAt.plus(Duration.ofSeconds(-command.deltaSeconds));
          return s.with({ restTimer: new RestTimer(newStartedAt, s.restTimer.pausedAt) });
        },
      }),
    );
  }

  private on<T extends WorkoutMessage['payload']>(type: T['type'], eventHandler: (e: T) => void) {
    const listener = this.listeners.get(type) ?? [];

    listener.push(eventHandler as (e: WorkoutMessage['payload']) => void);

    this.listeners.set(type, listener);
  }

  private getTranslationMessage(): Translations {
    return {
      workoutPersistentNotificationFinishWorkoutAction: this.tolgee.t(
        'workout_persistent_notification.finish_workout.action' satisfies TranslationKey,
      ),
      workoutPersistentNotificationInProgressMessage: this.tolgee.t(
        'workout_persistent_notification.in_progress.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationRestBreakMessage: this.tolgee.t(
        'workout_persistent_notification.rest_break.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationStartNowMessage: this.tolgee.t(
        'workout_persistent_notification.start_now.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationStartSoonMessage: this.tolgee.t(
        'workout_persistent_notification.start_soon.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationFinishedMessage: this.tolgee.t(
        'workout_persistent_notification.finished.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationCurrentExerciseMessage: this.tolgee.t(
        'workout_persistent_notification.current_exercise.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationMinRestOverMessage: this.tolgee.t(
        'workout_persistent_notification.min_rest_over.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationMaxRestOverMessage: this.tolgee.t(
        'workout_persistent_notification.max_rest_over.message' satisfies TranslationKey,
      ),
      workoutPersistentNotificationCardioTargetReachedMessage: this.tolgee.t(
        'workout_persistent_notification.cardio_target_reached.message' satisfies TranslationKey,
      ),
    };
  }
}
