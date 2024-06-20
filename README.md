# DermaFace-An-App-For-Your-Facial-Skin-and-Care

Facial skin conditions, especially among teenagers, often disrupt daily activities and diminish self-confidence. To address this issue, we developed a mobile-based application designed to help users assess their facial conditions and provide tips on how to care for their skin. This app uses machine learning techniques for facial disease detection and offers a user-friendly interface to enhance accessibility and usability.

**Machine Learning Part**

We built our facial disease detection model using machine learning techniques. Specifically, we utilized the MobileNetV2 architecture through transfer learning, customizing it to improve detection accuracy for various facial skin diseases. The model was trained to recognize and classify different skin diseases, providing users with reliable and accurate assessments. The model is then converted into tensorflow lite so that it can be deployed into mobile applications.

**Mobile Development Part** 

The application was developed using Kotlin, ensuring a seamless and responsive user experience. TensorFlow Lite was deployed to integrate the machine learning model within the mobile app, enabling detection for images from gallery or taken by phone camera. We implemented Firebase as the backend database to store all the data that is required in the app.

Our face disease detection app successfully integrates machine learning and mobile development for development and deployment. The software addresses a major problem that users and teenagers encounter and offers a useful tool for enhancing skin health and confidence. 

