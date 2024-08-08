from ninja import NinjaAPI, File, UploadedFile, Form
from gradio_client import Client, handle_file
from PIL import Image
import numpy as np
import tensorflow as tf

# from decouple import config     #if using your own token

from .models import ImageDetection

import pathlib
import sys
import os
import re

import subprocess


api = NinjaAPI()
print(tf.__version__)
print(np.__version__)

print("Before: ")
print("OS Path: ", os.path)
print("Sys Path: ", sys.path)

print("Big_Vision_path")
print(os.path.exists("big_vision_repo"))

if not os.path.exists("big_vision_repo"):
  subprocess.run(["git", "clone", "--quiet", "--branch=main", "--depth=1", "https://github.com/google-research/big_vision", "big_vision_repo"], check=True)
  print("Cloned the repo.")

if "big_vision_repo" not in sys.path:
  sys.path.append("big_vision_repo")
  print("Added Big_Vision_Repo to path")

print("After: ")
print(os.path.exists("big_vision_repo"))

import big_vision_repo.big_vision.evaluators.proj.paligemma.transfers.segmentation as segeval
reconstruct_masks = segeval.get_reconstruct_masks('oi')

def normalize_coordinates(coord: str, img_x, img_y):
    detect_pattern = r'<loc(\d+)>'
    detect_matches = re.findall(detect_pattern, coord)
    
    # Normalize the coordinates.
    numbers = [int(detect_match) for detect_match in detect_matches]
    numbers[0] = int((numbers[0] / 1024) * img_y)
    numbers[1] = int((numbers[1] / 1024) * img_x)
    numbers[2] = int((numbers[2] / 1024) * img_y)
    numbers[3] = int((numbers[3] / 1024) * img_x)

    return numbers

def parse_segmentation(coord: str, img_x, img_y):
    detect_pattern = r'<loc(\d+)>'
    detect_matches = re.findall(detect_pattern, coord)
    
    # Normalize the coordinates.
    numbers = [int(detect_match) for detect_match in detect_matches]
    numbers[0] = int((numbers[0] / 1024) * img_y)
    numbers[1] = int((numbers[1] / 1024) * img_x)
    numbers[2] = int((numbers[2] / 1024) * img_y)
    numbers[3] = int((numbers[3] / 1024) * img_x)

    # Parse segmentation key points.
    segment_pattern = r'<seg(\d+)>'
    segment_matches = re.findall(segment_pattern, coord)
    segs = []
    segments = [int(segment_match) for segment_match in segment_matches]
    segs.append(segments)

    mask = np.array(reconstruct_masks(np.array(segs)))

    return numbers, mask[0]
    

@api.post('/detect')
def detect(request, prompt: Form[str], image: File[UploadedFile], width: Form[int], height: Form[int]):
    prompt = prompt.lower()
    prompt_word = prompt.split()
    client = Client("big-vision/paligemma")
    prompt_obj = ImageDetection.objects.create(
        prompt=prompt,
        image=image
    )
    cwd = pathlib.Path(os.getcwd())
    image_path = pathlib.Path(prompt_obj.image.url[1:]) #skipping the forward slash so pathlib doesnt consider it an absolute url
    img_path = pathlib.Path(cwd , image_path)
    media_path = os.getcwd() + '/media/images/'

    # Resize image with width, height parameters.
    img = Image.open(img_path)
    img = img.convert('RGB')
    resized_img = img.resize((width, height), Image.Resampling.LANCZOS)
    resized_img_path = media_path + 'resized_' + str(image)
    resized_img.save(resized_img_path)

    result = client.predict(
    handle_file(resized_img_path),
    prompt,
    "paligemma-3b-mix-224", # str in 'Prompt' Textbox component # Literal[] in 'Model' Dropdown component
    "greedy", # Literal['greedy', 'nucleus(0.1)', 'nucleus(0.3)', 'temperature(0.5)'] in 'Decoding' Dropdown component
    api_name="/compute"
    )

    print(result)

    # Delete images after processing.
    [os.remove(os.path.join(media_path, f)) for f in os.listdir(media_path) if os.path.isfile(os.path.join(media_path, f))]
    
    data = result[0]["value"]
    img_x = result[2]["width"]
    img_y = result[2]["height"]

    """
    # create a list of objects detected
    [
        {
            "object": "car",
            "coordinates": [y1 x1 y2 x2]
        }
    ]
    """
    container = []
    if len(data) == 0:
        errors = {}
        errors["error"] = "Detection not found."
        errors["result"] = None
        return errors
    else:
        for object in data:
            temp = {}
            if prompt_word[0] == "detect":
                temp["label"] = object["class_or_confidence"]
                temp['coordinates'] = normalize_coordinates(object["token"], img_x, img_y)
                container.append(temp)
            elif prompt_word[0] == "segment":
                bbox, seg_output = parse_segmentation(object["token"], img_x, img_y)
                y1, x1, y2, x2 = bbox[0], bbox[1], bbox[2], bbox[3]
                mask_width = x2 - x1
                mask_height = y2 - y1
                x_scale = mask_width / 64
                y_scale = mask_height / 64

                x_coords = np.arrange(mask_width)
                y_coords = np.arrange(mask_height)
                x_coords = (x_coords / x_scale).astype(int)
                y_coords = (y_coords / y_scale).astype(int)
                
                resized_segment_mask = seg_output[y_coords[:, np.newaxis], x_coords]
                resized_segment_mask = np.squeeze(resized_segment_mask)

                x_mask = []
                y_mask = []
                for y in range(mask_height):
                    for x in range(mask_width):
                        if resized_segment_mask[y, x] > 0:
                            x_mask.append(x)
                            y_mask.append(y)

                temp["label"] = object["class_or_confidence"]
                temp["mask_x"] = x_mask
                temp["mask_y"] = y_mask
                container.append(temp)
            else:
                temp["response"] = object["token"]
        if "detect" in prompt:
            return {"result": container}
        elif "segment" in prompt:
            a = {"segment": container}
            print(a)
            return {"segment": container}
        else:
            return temp
               
            
    
