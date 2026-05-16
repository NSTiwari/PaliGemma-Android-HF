# PaliGemma Android HF
An Android app that lets you point your phone camera at anything and ask PaliGemma questions about it — detect objects, read text, describe scenes, or segment specific regions, all from a native Android UI backed by a Django REST API.

## Pipeline

<img src="https://github.com/NSTiwari/PaliGemma-Android-HF/blob/main/assets/paligemma_android_hf_pipeline.png"/>

## How it works

The app captures or picks an image, sends it to a Django server along with a text prompt, and displays the result. The server figures out which model and task to use based on the prompt, calls the appropriate Hugging Face Space via the Gradio client, and returns structured data back to the app.

Two models are wired up behind a single API endpoint:

- **PaliGemma** (via `big-vision/paligemma` HF Space) handles object detection, image captioning, OCR, and visual Q&A
- **Florence-2** (via `gokaygokay/Florence-2` HF Space) handles reference expression segmentation — you can pass multiple labels separated by semicolons and get back polygon coordinates for each

The routing is prompt-based: if the prompt starts with `segment`, the request goes to Florence-2; everything else goes to PaliGemma.

**Coordinate handling** is done on the server side. PaliGemma returns bounding boxes in its internal 1024×1024 coordinate space as `<loc>` tokens. The Django view parses these tokens, normalizes them to the actual image dimensions, and returns clean pixel coordinates that the Android app draws directly onto the image using Jetpack Compose's `Canvas` API.

For segmentation, Florence-2 returns polygon point arrays per label. The app renders these as overlaid polygon outlines on top of the captured image.

## Architecture

**Android (Kotlin + Jetpack Compose)**
- MVVM with a ViewModel, Repository interface, and Retrofit-based API layer
- Camera capture and gallery picker with runtime permission handling
- Canvas-based bounding box and polygon rendering directly on the displayed image
- Coroutine-driven async requests with loading state and error handling via Snackbar

**Python Server (Django + Django-Ninja)**
- Single `/detect` POST endpoint that accepts multipart image upload + form fields
- Image is resized server-side using Pillow before being forwarded to HF
- Gradio client handles the HF Space calls; results are cleaned and returned as JSON
- Temporary images are deleted after each request to avoid disk buildup

## Demo Outputs

**Visual question-answering, zero-shot object detection, image captioning**

<img src="https://github.com/NSTiwari/PaliGemma-Android-HF/blob/main/assets/paligemma-android-hf1.gif"/>

**Reference Expression Segmentation**

Model used: [Florence-2](https://huggingface.co/microsoft/Florence-2-large)

<img src="https://github.com/NSTiwari/PaliGemma-Android-HF/blob/main/assets/paligemma-android-segmentation.gif" width="400"/>

## How to run

### Python server

```bash
cd Python_Server/apiserver
pip install django django-ninja gradio-client Pillow numpy
python manage.py migrate
python manage.py runserver 0.0.0.0:8000
```

### Android app

1. Open `Android_App` in Android Studio
2. In `CoordinatesModelApi.kt`, update the base URL to point to your server's IP address
3. Build and run on a physical device or emulator (API 26+)
4. Grant camera permission when prompted

The app works over a local network or any publicly accessible server URL — no cloud account needed on the Android side.

## Resources:
- [Colab notebooks for PaliGemma](https://github.com/NSTiwari/PaliGemma)
- [Official Gemma Cookbook](https://github.com/google-gemini/gemma-cookbook)
- [Medium blog](https://tiwarinitin1999.medium.com/paligemma-on-android-using-hugging-face-api-7716ec262837) for step-by-step implementation.
- [Big Vision HF 🤗 Spaces](https://huggingface.co/spaces/big-vision/paligemma)

## Citation
If you find this project useful for your work, please cite it using the following BibTeX entry:

```
@misc{PaliGemma on Android using Hugging Face API,
  authors      = {Nitin Tiwari, Sagar Malhotra, Savio Rodrigues},
  title        = {PaliGemma on Android using Hugging Face API},
  year         = {2024},
  publisher    = {GitHub},
  howpublished = {\url{https://github.com/NSTiwari/PaliGemma-Android-HF}},
}
```

## Acknowledgment
<img src="https://github.com/NSTiwari/PaliGemma-Android-HF/blob/main/assets/google.png">

This project was developed during Google's ML Developer Programs AI Sprint. Thanks to the MLDP team for providing Google Cloud credits to support this project.

