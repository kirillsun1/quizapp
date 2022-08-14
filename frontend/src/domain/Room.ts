export interface CurrentQuestion {
  text: string;
  answers: string[];
}

export enum OngoingQuizStatus {
  NOT_STARTED = 'NOT_STARTED',
  QUESTION_IN_PROGRESS = 'QUESTION_IN_PROGRESS',
  WAITING = 'WAITING',
  DONE = 'DONE'
}

export interface OngoingQuiz {
  currentQuestion: CurrentQuestion | undefined;
  points: { [playerName: string]: number },
  status: OngoingQuizStatus
}

export interface Room {
  code: string;
  ongoingQuiz?: OngoingQuiz;
  moderator: string;
  players: string[];
}