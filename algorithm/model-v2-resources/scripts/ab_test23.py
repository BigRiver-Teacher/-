import cv2
from ultralytics import YOLO
old = YOLO(r'D:/fall-detection-data/weights/yolo11n-pose.pt')
new = YOLO(r'D:/fall-detection-data/weights/yolo11n-pose-fallen.pt')

def det(model, frame, conf):
    r = model.predict(frame, conf=conf, verbose=False)[0]
    b = r.boxes
    if b is None or len(b)==0: return []
    return [(round(float(c),2), round((float(x2)-float(x1))/max(float(y2)-float(y1),1),2)) for (x1,y1,x2,y2),c in zip(b.xyxy.tolist(), b.conf.tolist())]

for vname, frames in [('02_推车老人跌倒', list(range(10,145,15))), ('03_老人洗衣服台阶跌倒', [30,40,45,50,60,80,120,200,400,800,1100])]:
    cap = cv2.VideoCapture(f'D:/fall-detection-data/user_videos/{vname}_v2.mp4')
    print(f"===== {vname} =====")
    for fn in frames:
        cap.set(cv2.CAP_PROP_POS_FRAMES, fn)
        ok, frame = cap.read()
        if not ok: continue
        o3 = det(old, frame, 0.3); n3 = det(new, frame, 0.3)
        print(f" f{fn:4d} OLD0.3={o3 if o3 else 'none'}   NEW0.3={n3 if n3 else 'none'}")
    cap.release()
