import React from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { State } from '../state/store'
import {
  QBox,
  QButton,
  QCenter,
  QCircularProgress,
  QContainer,
  QFlex,
  QHeading,
  QScaleFade,
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
    <QContainer maxW={'container.lg'} minH={'100vh'}>
      <QStack paddingTop={12} gap={6}>
        <QBox
          background={'white'}
          paddingY={8}
          paddingX={16}
          borderRadius={36}
          boxShadow="xl"
        >
          <QFlex
            direction={'row'}
            justifyContent={'space-between'}
            alignItems={'center'}
            wrap={'wrap'}
          >
            <QBox>
              <QHeading marginBottom={2}>Room</QHeading>
            </QBox>

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
      </QStack>
    </QContainer>
  )
}

const Activity = ({quizStatus}: { quizStatus: OngoingQuizStatus }) => {
  switch (quizStatus) {
    case OngoingQuizStatus.NOT_STARTED:
      return <Pause status={'notStarted'}/>
    case OngoingQuizStatus.QUESTION_IN_PROGRESS:
      return <QuestionInProgress/>
    case OngoingQuizStatus.WAITING:
      return <Pause status={'betweenRounds'}/>
    case OngoingQuizStatus.DONE:
      return <Pause status={'done'}/>
    default:
      return <QText>?</QText>
  }
}

const pauseMessages = {
  'notStarted': 'Quiz will start soon',
  'betweenRounds': 'Wait for the next question',
  'done': 'Quiz finished! Thank you for participation!',
}

const Pause = ({status}: { status: 'notStarted' | 'betweenRounds' | 'done' }) =>
  <QBox
    background={'white'}
    paddingY={8}
    paddingX={16}
    borderRadius={36}
    boxShadow="xl"
  >
    <QStack>
      {
        status !== 'done' ? <QCenter> <QCircularProgress mr={2} isIndeterminate color="green.400"/> </QCenter> : null
      }
      <QCenter>
        <QHeading size={'md'}>{pauseMessages[status]}</QHeading>
      </QCenter>
    </QStack>
  </QBox>


function QuestionInProgress() {
  const question = useSelector((state: State) => state.game.room?.ongoingQuiz?.currentQuestion)
  const votingInProgress = useSelector((state: State) => state.game.loadings.voting)
  const selectedAnswer = useSelector((state: State) => state.game.currentAnswer)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const vote = (answer: number) => {
    if (answer !== selectedAnswer) {
      dispatch(actions.vote(answer))
    }
  }

  if (!question) {
    return null
  }

  return (
    <QScaleFade in={!!question} initialScale={0.7}>
      <QBox
        background={'white'}
        padding={6}
        borderRadius={36}
        boxShadow="xl"
      >
        <QStack spacing={8}>
          <QBox
            background={'cyan.500'}
            color={'cyan.50'}
            padding={6}
            borderRadius={20}
          >
            <QCenter>
              <QHeading size={'lg'}>{question.text}</QHeading>
            </QCenter>
          </QBox>
          <QSimpleGrid columns={2} spacing={10}>
            {
              question.answers.map((answer, index) =>
                <QButton
                  key={answer}
                  background={index === selectedAnswer ? 'cyan.500' : 'green'}
                  size={'lg'}
                  onClick={() => vote(index)}
                  borderRadius={20}
                  isLoading={votingInProgress && index === selectedAnswer}
                  disabled={votingInProgress}
                >
                  {answer}
                </QButton>,
              )
            }
          </QSimpleGrid>
        </QStack>
      </QBox>
    </QScaleFade>
  )
}