import cv2
import time
import Hand_Tracking_Module as htm
from ctypes import cast, POINTER
from comtypes import CLSCTX_ALL
import numpy as np
from pycaw.pycaw import AudioUtilities, IAudioEndpointVolume

pTime = 0
cTime = 0
video = cv2.VideoCapture(0)

detector = htm.Hand_Detector(min_detection_confidence=0.8)

devices = AudioUtilities.GetSpeakers()
interface = devices.Activate(IAudioEndpointVolume._iid_, CLSCTX_ALL, None)
volume = cast(interface, POINTER(IAudioEndpointVolume))

# volume.GetMute()
# volume.GetMasterVolumeLevel()

minVol, maxVol, _ = volume.GetVolumeRange()
c = 0
# volume.SetMasterVolumeLevel(-63.5, None)

while True:

    try:
        while True:
            success, img = video.read()
            if not success:
                break

            image = detector.findHands(img)

            try:
                points = detector.find_positions(image, 0)
            
            except:
                points = []

            try:    
                points1 = detector.find_positions(image, 1)
            except:
                points1 = []

            if len(points1) != 0:
                x4_hand1, y4_hand1 = points1[4][1], points1[4][2]
                x20_hand1, y20_hand1 = points1[20][1], points1[20][2]

                cv2.circle(img, (x4_hand1, y4_hand1), 20, (0, 0, 0), cv2.FILLED)
                cv2.circle(img, (x20_hand1, y20_hand1), 20, (0, 0, 0), cv2.FILLED)

                length = int(pow(pow(x4_hand1 - x20_hand1, 2) + pow(y4_hand1 - y20_hand1, 2), 1 / 2))

                if length > 40:
                    cv2.circle(img, (x4_hand1, y4_hand1), 20, (37, 173, 225), cv2.FILLED)
                    cv2.circle(img, (x20_hand1, y20_hand1), 20, (37, 173, 225), cv2.FILLED)
                    c = 0
                    break

                else:
                    c = 1

            cTime = time.time()
            fps = 1 / (cTime - pTime)
            pTime = cTime
            
            image = cv2.flip(image, 1) 

            cv2.putText(image, f'FPS: {str(int(fps))}', (10, 70), cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 0, 255), 5)

            cv2.imshow("Image", image)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                exit(0)

    except Exception as e:
        # c = 1

        print (f"Exception = {e}")
        continue

    if len(points) != 0 and c == 0:
        # print (points[4], points[8])

        x4, y4 = points[4][1], points[4][2]
        x8, y8 = points[8][1], points[8][2]

        cv2.circle(image, (x4, y4), 10, (255, 0, 255), cv2.FILLED)
        cv2.circle(image, (x8, y8), 10, (255, 0, 255), cv2.FILLED)
        cv2.circle(image, ((x4 + x8) // 2, (y4 + y8) // 2), 10, (255, 0, 255), cv2.FILLED)

        cv2.line(image, (x4, y4), (x8, y8), (255, 0, 255), 3)

        length = int(pow(pow(x4 - x8, 2) + pow(y4 - y8, 2), 1 / 2))
        
        # Hand range: 30 - 275
        # Volume range: -63.5 - 0

        if length < 30:
            cv2.circle(image, ((x4 + x8) // 2, (y4 + y8) // 2), 10, (0, 255, 0), cv2.FILLED)

        if length >= 150:
            cv2.circle(image, (x4, y4), 10, (0, 139, 200), cv2.FILLED)
            cv2.circle(image, (x8, y8), 10, (0, 139, 200), cv2.FILLED)

        vol = np.interp(length, [30, 150], [minVol, maxVol])
        volume.SetMasterVolumeLevel(vol, None)

        print (f"Volume: {int(np.interp(vol, [minVol, maxVol], [0, 100]))}")

    cTime = time.time()
    
    # print (f"Ctime = {cTime}, ptime = {pTime}")
    fps = 1 / (cTime - pTime)
    pTime = cTime

    image = cv2.flip(image, 1) 
    cv2.putText(image, f'FPS: {str(int(fps))}', (10, 70), cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 0, 255), 5)
    cv2.imshow("Image", image)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

video.release()
cv2.destroyAllWindows()