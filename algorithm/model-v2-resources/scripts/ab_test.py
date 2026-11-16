import cv2
from ultralytics import YOLO

video = r'D:/fall-detection-data/user_videos/01_台阶跌倒老人_v2.mp4'
old = YOLO(r'D:/fall-detection-data/weights/yolo11n-pose.pt')
new = YOLO(r'D:/fall-detection-data/weights/yolo11n-pose-fallen.pt')

cap = cv2.VideoCapture(video)
frames = [40, 60, 67, 80, 100, 130, 180, 250, 400, 700, 1100, 1500]
def det(model, frame, conf):
    r = model.predict(frame, conf=conf, verbose=False)[0]
    boxes = r.boxes
    if boxes is None or len(boxes) == 0:
        return []
    out = []
    for i in range(len(boxes)):
        x1,y1,x2,y2 = boxes.xyxy[i].tolist()
        w = x2-x1; h = y2-y1
        out.append((round(float(boxes.conf[i]),2), round(w/max(h,1),2)))
    return out

for fn in frames:
    cap.set(cv2.CAP_PROP_POS_FRAMES, fn)
    ok, frame = cap.read()
    if not ok: continue
    line = f"frame {fn:4d}: "
    line += "OLD(0.3)=" + (str(det(old,frame,0.3)) if det(old,frame,0.3) else "none")
    line += " | OLD(0.15)=" + (str(det(old,frame,0.15)) if det(old,frame,0.15) else "none")
    line += " | NEW(0.3)=" + (str(det(new,frame,0.3)) if det(new,frame,0.3) else "none")
    line += " | NEW(0.15)=" + (str(det(new,frame,0.15)) if det(new,frame,0.15) else "none")
    print(line)
cap.release()
