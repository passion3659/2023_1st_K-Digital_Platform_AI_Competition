from flask import Flask, render_template, request, jsonify
from waitress import serve
import os

import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as transforms
import transformers
from PIL import Image

torch.backends.cudnn.benchmark = True
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")


app = Flask(__name__)

# 사용할 모델 선언
class VQAModel(nn.Module):
    def __init__(self, num_target, dim_i, dim_q, dim_h=1024):
        super(VQAModel, self).__init__()

        #The BERT model: 질문 --> Vector 처리를 위한 XLM-Roberta모델 활용
        self.bert = transformers.XLMRobertaModel.from_pretrained('xlm-roberta-base')

        #Backbone: 이미지 --> Vector 처리를 위해 ResNet50을 활용
        self.resnet = models.resnet50(pretrained=True)
        self.resnet.fc = nn.Linear(self.resnet.fc.in_features, dim_i)
        self.i_relu = nn.ReLU()
        self.i_drop = nn.Dropout(0.2)

        #classfier: MLP기반의 분류기를 생성
        self.linear1 = nn.Linear(dim_i, dim_h)
        self.q_relu = nn.ReLU()
        self.linear2 = nn.Linear(dim_h, num_target)
        self.q_drop = nn.Dropout(0.2)


    def forward(self, idx, mask, image):

        #_, q_f = self.bert(idx, mask) #질문을 Bert를 활용해 Vector화
        outputs = self.bert(idx, mask) #질문을 Bert를 활용해 Vector화
        i_f = self.i_drop(self.resnet(image)) # 이미지를 resnet을 활용해 Vector화
        q_f = outputs.pooler_output
        uni_f = i_f*q_f #이미지와 질문 vector를 point-wise연산을 통해 통합 vector생성

        return self.linear2(self.q_relu(self.linear1(uni_f))) #MLP classfier로 답변 예측

#질의를 처리할 BERT Tokenizer선언
tokenizer = transformers.XLMRobertaTokenizer.from_pretrained('xlm-roberta-base')

#이미지 전처리를 위한 이미지 크기 변환 및 각도조정을 위한 transform 선언
transform = transforms.Compose(
    [
        transforms.Resize((356, 356)),
        transforms.RandomCrop((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
    ]
)

# 체크포인트 로드
checkpoint = torch.load("model_last.tar", map_location=torch.device('cpu'))

answer_list = checkpoint['answer_list']

#모델 생성
model = VQAModel(num_target=len(answer_list), dim_q=768, dim_i=768, dim_h=1024)
model = torch.nn.DataParallel(model)

# 모델 상태 로드
model.load_state_dict(checkpoint['state_dict'])

def answering(model, img_file, question, tokenizer, answer_list, device):

    transform = transforms.Compose(
        [
            transforms.Resize((356, 356)),
            transforms.RandomCrop((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225]),
        ]
    )

    model.eval()
    img = transform(Image.open(img_file).convert("RGB")).unsqueeze(0)
    img = img.to(device)

    encoded = tokenizer.encode_plus("".join(question),
                                     None,
                                     add_special_tokens=True,
                                     max_length = 30,
                                     truncation=True,
                                     pad_to_max_length = True)

    with torch.no_grad():
        ids, mask = encoded['input_ids'], encoded['attention_mask']
        ids = torch.tensor(ids, dtype=torch.long).unsqueeze(0).to(device)
        mask = torch.tensor(mask, dtype=torch.long).unsqueeze(0).to(device)
        output = model(ids, mask, img)
        #print(output)

        predicted = torch.argmax(output, dim=1).item()
        #print(predicted)
        answer = answer_list['ANSWER'][predicted]

    return answer
print('ready')


@app.route('/')
def index():
    return render_template("putImage.html")


@app.route('/', methods=['POST'])
def practice():
    try:
        # 텍스트 데이터 가져오기
        question = request.form["question"]
        print(question)

        # 업로드 파일 가져오기
        imageFile = request.files['imageFile']
        print(imageFile)

        # file 없으면 에러
        if imageFile.filename == '':
            return jsonify({'error': 'No selected file'})

        # 폴더 없으면 만들기
        upload_folder = os.path.join(os.path.dirname(__file__), "upload_images")
        if not os.path.exists(upload_folder):
            os.makedirs(upload_folder)

        # 업로드 경로 지정 및 파일 저장
        image_path = os.path.join(upload_folder, imageFile.filename)
        imageFile.save(image_path)

        # 저장된 파일 경로
        print("이미지 파일 경로:", image_path)

        # 이미지 전처리
        image = imageFile

        answer = answering(model, image_path, question, tokenizer, answer_list, DEVICE)

        return render_template("putImage.html", prediction=answer)

        # 이 코드는 모델 다 완성되고 테스트 해볼 때 사용
        # return render_template("putImage.html", prediction=result)

        # 최종적으로 json 파일로 나오도록
        # # json 파일 형태로 저장 (예시임)
        # response_data = {'question': question,
        #                  'result': result}
        # # json 파일로 반환
        # return jsonify(response_data)

    except Exception as e:
        return jsonify({'error': str(e)})


# 이 부분은 너꺼 완성되면 최종 엔드포인트로 사용할 부분
@app.route('/predict', methods=['POST'])
def predict():
    try:
        # 텍스트 데이터 가져오기
        question = request.form["question"]
        print(question)

        # 업로드 파일 가져오기
        imageFile = request.files['imageFile']
        print(imageFile)

        # file 없으면 에러
        if imageFile.filename == '':
            return jsonify({'error': 'No selected file'})

        # 폴더 없으면 만들기
        upload_folder = os.path.join(os.path.dirname(__file__), "upload_images")
        if not os.path.exists(upload_folder):
            os.makedirs(upload_folder)

        # 업로드 경로 지정 및 파일 저장
        image_path = os.path.join(upload_folder, imageFile.filename)
        imageFile.save(image_path)

        # 저장된 파일 경로
        print("이미지 파일 경로:", image_path)

        # 이미지 전처리
        image = imageFile

        # 모델
        # yhat = model.predict(image)

        # 결과값 변수로 해서
        # result = model.predict(image)

        # json 파일 형태로 저장
        # response_data = {'result': result}

        # json 파일로 반환
        # return jsonify(response_data)

    except Exception as e:
        return jsonify({'error': str(e)})


if __name__ == "__main__":
    # 서버에서 돌릴 때
    serve(app, host="0.0.0.0", port="5000")
    # 디버그 모드 -> 로컬에서 돌릴 때
    # app.run(debug=True, port=5000)
