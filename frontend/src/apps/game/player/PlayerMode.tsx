import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { State } from '../state/store'
import {
  QBox,
  QButton,
  QCircularProgress,
  QContainer,
  QFlex,
  QHeading,
  QSimpleGrid,
  QStack,
  QText,
} from '../../../core/components'
import { OngoingQuizStatus } from '../../../domain/Room'
import { SelectRoom } from './SelectRoom'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { actions } from '../state/gameSlice'

export function PlayerMode() {
  const room = useSelector((state: State) => state.game.room)
  const roomStatus = useSelector((state: State) => state.game.room?.ongoingQuiz?.status ?? OngoingQuizStatus.NOT_STARTED)
  if (!room) {
    return <SelectRoom/>
  }

  return (
    <QContainer maxW={'container.lg'}>
      <QBox
        borderRadius={12}
        color={'darkgreen'}
        marginTop={12}
        marginBottom={6}
      >
        <QFlex
          direction={'row'}
          justifyContent={'space-between'}
          alignItems={'center'}
          wrap={'wrap'}
        >
          <QHeading>
            Welcome to the room!
          </QHeading>
          <QFlex alignItems={'center'}>
            <QBox mr={12}>
              <QText fontWeight="bold">{room.code}</QText>
              <QText fontSize="sm">Room</QText>
            </QBox>
            <QBox mr={12}>
              <QText fontWeight="bold">{room.moderator}</QText>
              <QText fontSize="sm">Moderator</QText>
            </QBox>
            <QBox mr={12}>
              <QText fontWeight="bold">{room.players.length}</QText>
              <QText fontSize="sm">Player(s)</QText>
            </QBox>
          </QFlex>
        </QFlex>
      </QBox>

      <Activity quizStatus={roomStatus}/>
    </QContainer>
  )
}

const Activity = ({quizStatus}: { quizStatus: OngoingQuizStatus }) => {
  switch (quizStatus) {
    case OngoingQuizStatus.NOT_STARTED:
      return <NotStarted/>
    case OngoingQuizStatus.QUESTION_IN_PROGRESS:
      return <QuestionInProgress/>
    default:
      return <QText>Done</QText>
  }
}

const NotStarted = () =>
  <QStack spacing={10}>
    <QFlex alignItems={'center'}>
      <QCircularProgress mr={2} isIndeterminate/>
      <QHeading size={'md'}>Please wait, moderator will start the quiz soon</QHeading>
    </QFlex>
    <QText>Invite more friends to have fun!</QText>
  </QStack>

function QuestionInProgress() {
  const question = useSelector((state: State) => state.game.room?.ongoingQuiz?.currentQuestion)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const vote = (answer: number) => {
    dispatch(actions.vote(answer))
  }

  if (!question) {
    return null
  }

  return (
    <QBox
      background={'blue.50'}
      color={'darkblue'}
      padding={4}
      borderRadius={4}
    >
      <QStack spacing={4}>
        <QHeading size={'md'}>Your question is:</QHeading>
        <QBox
          background={'white'}
          padding={5}
        >
          <QHeading>{question.text}</QHeading>
        </QBox>
        <QSimpleGrid columns={2} spacing={'10'}>
          {
            question.answers.map((answer, index) =>
              <QButton
                key={answer}
                colorScheme={'blue'}
                size={'lg'}
                onClick={() => vote(index)}
              >
                {answer}
              </QButton>)
          }
        </QSimpleGrid>
      </QStack>
    </QBox>
  )
}