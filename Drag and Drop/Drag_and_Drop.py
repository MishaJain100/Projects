import cv2
import Hand_Tracking_Module as htm

video = cv2.VideoCapture(0)
video.set(3, 1280)
video.set(4, 720)
detector = htm.Hand_Detector(min_detection_confidence=0.8)

cx, cy = 100, 100
w, h = 200, 200
colour = (255, 0, 255)

while True:
    _, image = video.read()
    image = detector.findHands(image)
    points = detector.find_positions(image)

    cv2.rectangle(image, (cx-w//2, cy-h//2), (cx+w//2, cy+h//2), colour, cv2.FILLED)
    cv2.rectangle(image, (cx-w//2 - 20, cy-h//2 - 20), (cx+w//2 + 20, cy+h//2 + 20), (0, 255, 0))

    if len(points) != 0:
        x8, y8 = points[8][1], points[8][2]
        x12, y12 = points[12][1], points[12][2]
    
        if x8 > cx-w//2 - 20 and x8 < cx+w//2 + 20 and y8 > cy-h//2 - 20 and y8 < cy+h//2 + 20:

            colour = (37, 72, 43)
            length = int(pow(pow(x8 - x12, 2) + pow(y8 - y12, 2), 1 / 2))

            if length < 50:
                cx, cy = x8, y8
                colour = (255, 255, 255)

            else:
                colour = (37, 72, 43)
        
        else:
            colour = (255, 0, 255)
    
    else:
        colour = (255, 0, 255)
                

    image = cv2.flip(image, 1)
    cv2.imshow("Image", image)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break