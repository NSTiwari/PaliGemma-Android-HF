from ninja import NinjaAPI, File, UploadedFile, Form
from gradio_client import Client, handle_file
from PIL import Image

# from decouple import config     #if using your own token

from .models import ImageDetection

import pathlib

import os
import re


api = NinjaAPI()

def normalize_coordinates(coord: str, img_x, img_y):
    pattern = r'<loc(\d+)>'
    matches = re.findall(pattern, coord)
    # The coordinates also need to be normalized i.e., divide by 1024 and then multiple by height, width of the original image.
    
    numbers = [int(match) for match in matches]
    numbers[0] = int((numbers[0] / 1024) * img_y)
    numbers[1] = int((numbers[1] / 1024) * img_x)
    numbers[2] = int((numbers[2] / 1024) * img_y)
    numbers[3] = int((numbers[3] / 1024) * img_x)
    return numbers

@api.post('/detect')
def detect(request, prompt: Form[str], image: File[UploadedFile], width: Form[int], height: Form[int]):
    # Resize image.
    print("Width: ", width)
    print("Height: ", height)
    print(image)
    # img = Image.open(image).resize((width, height))
    # print(img.size)
    # Resize the image.
  
    # img = img.resize((width, height), Image.Resampling.LANCZOS)
    # img_name = str(image) + '_resized.jpg'
    # temp_image_path = pathlib.Path(os.getcwd() + img_name)
    # img.save(temp_image_path)
    
    client = Client("big-vision/paligemma")
    prompt_obj = ImageDetection.objects.create(
        prompt=prompt,
        image=image
    )
    cwd = pathlib.Path(os.getcwd())
    image_path = pathlib.Path(prompt_obj.image.url[1:]) #skipping the forward slash so pathlib doesnt consider it an absolute url
    img_path = pathlib.Path(cwd , image_path)

    img = Image.open(img_path)
    print(img_path)
    resized_img = img.resize((width, height), Image.Resampling.LANCZOS)
    # resized_img_name = 'resized.jpg'
    temp_path = os.getcwd() + '/resized_img.jpg'
    print(temp_path)
    resized_img.save(temp_image_path)
    
    
    result = client.predict(
    handle_file(img_path),
    prompt,
    "paligemma-3b-mix-224", # str in 'Prompt' Textbox component # Literal[] in 'Model' Dropdown component
    "greedy", # Literal['greedy', 'nucleus(0.1)', 'nucleus(0.3)', 'temperature(0.5)'] in 'Decoding' Dropdown component
    api_name="/compute"
    )
    # print(result)

    # print(f"{result=}")
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
    for object in data:
        temp = {}
        temp["label"] = object["class_or_confidence"]
        temp['coordinates'] = normalize_coordinates(object["token"], img_x, img_y)
        container.append(temp)
    return {"result": container}
    
