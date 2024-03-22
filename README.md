# 제 1회 K-디지털 플랫폼 AI 경진대회
이 프로젝트는 "제 1회 K-디지털 플랫폼 AI 경진대회"에서 장려상을 수상한 데이터 분석 프로젝트입니다. 공모전의 주제는 AI기술을 활용하여 디지털에 대한 접근성을 향상시키고 디지털 분야에서 평등을 촉직하는 아이디어 제시 및 솔루션 개발입니다. 저희는 AI를 활용하여 시각장애인 및 색맹인들이 일상 생활에서 시각 정보에 더 쉽게 접근할 수 있는 어플리케이션을 제안했습니다.

본 프로젝트는 멀티모달 AI 기술을 활용하여 이미지와 자연어를 동시에 처리하는 Visual Question Answering(VQA) 모델을 개발 후 해당 모델을 적용한 어플리케이션을 만드는 것입니다. 먼저  이미지 처리를 위해 사전 훈련된 ResNet50 모델과 자연어 처리를 위해 사전 훈련된 XLM-RoBERTa 모델을 결합하여, 사용자가 질문한 내용에 대해 이미지 내에서 답변을 생성할 수 있는 AI 모델을 개발합니다. 그 뒤, Flask 웹 프레임워크를 활용해 서버를 구축하고, 안드로이드 스튜디오와 연동하여 사용자가 음성 인식이나 텍스트 입력을 통해 질문할 수 있는 인터페이스를 설계합니다. 그리고 음성을 텍스트로 (Speech-To-Text) 그리고 텍스트를 음성으로 (Text-To-Speech) 인식하는 기능을 추가하여 사용자 경험 개선을 위한 개발을 진행하였습니다. 

이 프로젝트는 시각적 불편함을 겪는 사람들의 디지털 접근성을 개선하고, 일상 생활에서의 독립성과 편의성을 향상시키는 데 기여할 것으로 기대됩니다. README 문서는 프로젝트의 개요, 목적, 사용된 기술, 개발 계획 등을 상세히 설명하여, 본 프로젝트가 시각장애인의 디지털 격차 해소에 어떻게 기여할 수 있는지를 명확하게 전달합니다.

본 프로젝트는 시각장애인 및 색맹인들을 포함한 더 넓은 사용자층의 디지털 접근성 문제 해결에 관심이 있는 연구자들과 개발자들에게 유용한 자료가 될 것입니다.

## 공모전 개요
  - <b>공모전 명칭</b>: 제 1회 K-디지털 플랫폼 AI 경진대회
  - <b>목적</b>: 인공지능을 활용한 디지털 격차 해소
    - AI기술을 활용하여 디지털에 대한 접근성을 향상시키고 디지털 분야에서 평등을 촉직하는 아이디어 제시 & 솔루션 개발
  - <b>주최</b>: 고용노동부, 한국산업인력공단, 한국기술교육대학교
  - <b>후원</b>: Microsoft 
  - <b>대회 일정</b>:
    - 참가접수 및 1차 선발 : 2023/11/03 ~ 2023/11/24
    - 온라인 사전 설명회 : 2023/12/01
    - AI-900 특강 : 2023/12/03
    - AI-900 시험 : 2023/12/06 ~ 2023/12/07
    - 해커톤 프로젝트 계획서 제출 : 2023/12/07
    - AI AZURE 특강 : 2023/12/10
    - AI 해커톤 경진대회 : 2023/12/12 ~ 2023/12/13
  - <b>공모전 사이트</b> : https://k-hp.co.kr/sub3.php?page=1 

## 공모전에서의 팀의 역할과 기여
- <b>팀 구성</b>
  - 팀명 : 앤드포인트
  - 팀장 : 조세은
  - 팀원 : 김가람, 이승준, 양정열
- <b>분석 배경</b>
  - 디지털 전환 시대에도 불구하고, 2022년 한 조사에 따르면 시각장애인 중 약 92%가 쇼핑 및 모바일 애플리케이션 사용에 어려움을 겪는다고 응답, 시각장애인의 디지털 접근성 문제가 심각함을 드러냄
  - 본 프로젝트는 AI 멀티모달 모델이 들어간 'ViewFinder' 애플리케이션 개발을 통해 시각장애인 및 색맹인 등 시각적 불편함을 겪는 사람들이 일상생활에서 시각 정보에 쉽게 접근할 수 있도록 지원, 디지털 격차를 해소하고자 함
- <b>본 팀에서의 기여</b>
  - 멀티모달 AI 기술을 활용한 Visual Question Answering(VQA) 모델 개발
  - 공공 데이터셋 활용 및 안드로이드 애플리케이션과 Flask 서버를 연동하는 애플리케이션 기술 구현
  - 사용자 경험 개선을 위한 TTS 및 STT 기능의 구현
