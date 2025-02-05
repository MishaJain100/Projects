import cv2
import mediapipe as mp
import time

class Hand_Detector():
    
    def __init__(self, mode=False, max_hands=2, min_detection_confidence=0.5, min_tracking_confidence=0.5):
        self.mode = mode
        self.max_hands = max_hands
        self.min_detection_confidence = min_detection_confidence
        self.min_tracking_confidence = min_tracking_confidence

        self.mpHands = mp.solutions.hands
        self.mpDraw = mp.solutions.drawing_utils
        self.hands = self.mpHands.Hands(
            static_image_mode=self.mode,
            max_num_hands=self.max_hands,
            min_detection_confidence=self.min_detection_confidence,
            min_tracking_confidence=self.min_tracking_confidence
        )

    def findHands(self, img, draw=True, draw_bb=True):
        imgRGB = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        self.results = self.hands.process(imgRGB)
        bb = False
        hands = False
        if self.results.multi_hand_landmarks:
            hands = True
            for hand in self.results.multi_hand_landmarks:
                if draw:
                    self.mpDraw.draw_landmarks(img, hand, self.mpHands.HAND_CONNECTIONS)

                    if draw_bb:
                        x_min = int(min(landmark.x * img.shape[1] for landmark in hand.landmark))
                        x_max = int(max(landmark.x * img.shape[1] for landmark in hand.landmark))
                        y_min = int(min(landmark.y * img.shape[0] for landmark in hand.landmark))
                        y_max = int(max(landmark.y * img.shape[0] for landmark in hand.landmark))
                        cv2.rectangle(img, (x_min - 20, y_min - 20), (x_max + 20, y_max + 20), (0, 255, 0), 2)
                        # bb = [x_min - 20, x_max + 20, y_min - 20, y_max + 20]

        return img
    
    def find_positions(self, img, handNo = 0):

        points = []

        if self.results.multi_hand_landmarks:
            myHand = self.results.multi_hand_landmarks[handNo]
            x_min = int(min(landmark.x * img.shape[1] for landmark in myHand.landmark))
            x_max = int(max(landmark.x * img.shape[1] for landmark in myHand.landmark))
            y_min = int(min(landmark.y * img.shape[0] for landmark in myHand.landmark))
            y_max = int(max(landmark.y * img.shape[0] for landmark in myHand.landmark))
            for id, lm in enumerate(myHand.landmark): #These are those 21 points that the hand has
                    
                # print (id, lm) #id is point number, lm is location of point
                    
                w = img.shape[1]
                h = img.shape[0]
                cx, cy = int(lm.x*w), int(lm.y*h) #x_center, y_center
                points.append([id, cx, cy])

                # if id == 0:
                #     cv2.circle(img, (cx, cy), 25, (255, 0, 255), cv2.FILLED)

                # print (id, cx, cy)
            
            bb = [x_min - 20, x_max + 20, y_min - 20, y_max + 20]

        return points

def main():
    pTime = 0
    cTime = 0
    video = cv2.VideoCapture(0)

    detector = Hand_Detector()

    while True:
        success, img = video.read()
        if not success:
            break

        img = detector.findHands(img)
        points = detector.find_positions(img)
        
        if len(points) != 0:
            print (points[0])

        cTime = time.time()
        fps = 1 / (cTime - pTime)
        pTime = cTime

        cv2.putText(img, str(int(fps)), (10, 70), cv2.FONT_HERSHEY_COMPLEX, 2, (255, 0, 255), 3)

        cv2.imshow("Image", img)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    video.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()

"""

    def find_positions(self, img, handNo = 0):

        points = []

        if self.results.multi_hand_landmarks:
            myHand = self.results.multi_hand_landmarks[handNo]
            x_min = int(min(landmark.x * img.shape[1] for landmark in myHand.landmark))
            x_max = int(max(landmark.x * img.shape[1] for landmark in myHand.landmark))
            y_min = int(min(landmark.y * img.shape[0] for landmark in myHand.landmark))
            y_max = int(max(landmark.y * img.shape[0] for landmark in myHand.landmark))
            for id, lm in enumerate(myHand.landmark): #These are those 21 points that the hand has
                    
                # print (id, lm) #id is point number, lm is location of point
                    
                w = img.shape[1]
                h = img.shape[0]
                cx, cy = int(lm.x*w), int(lm.y*h) #x_center, y_center
                points.append([id, cx, cy])

                # if id == 0:
                #     cv2.circle(img, (cx, cy), 25, (255, 0, 255), cv2.FILLED)

                # print (id, cx, cy)
            
            bb = [x_min - 20, x_max + 20, y_min - 20, y_max + 20]

        return points, bb

"""