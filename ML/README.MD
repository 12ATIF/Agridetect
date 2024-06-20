# Machine Learning part for DermaFace APP
We use tensorflow library to create image classfication model, train the model with our dataset, and then convert it to Tensorflow lite using post-training quantization.


The model is based on a pre-trained version of MobileNet V2 and we add some extra layers for increasing accuracy. The reason why using MobileNetV2 is because this model is popularly known to have a small size while still maintain good accuracy. Using this transfer learning approach help us to make a more reliable and faster model without training from the scratch. 

Once it's trained, we'll use post-training quantization to convert all parameters to int8 format, which reduces the model size and increases inferencing speed. This format is also required for compatibility on the TFlite in android.

# Notebook ML 
[Fix notebook](https://github.com/wahyuardiantito/DermaFace-An-App-For-Your-Facial-Skin-and-Care/blob/main/ML/Fix_Notebook.ipynb)

# Import the required libraries :
**Note**: This notebook requires TensorFlow 2.3+ for full quantization, which currently does not work for all types of models. In particular, this notebook expects a Keras-built model and this conversion strategy currently doesn't work with models imported from a frozen graph. 

* Tensorflow 
* Numpy
* Matplotlib

In order to quantize both the input and output tensors, we need TFLiteConverter APIs that are available in TensorFlow r2.3 or higher:

# Prepare the training data
Our dataset obtained by web scarping image in internet. The dataset contains 11 class, here's the link for dataset
[dataset](https://drive.google.com/drive/folders/1xUcEMdr4LD6tqIEwdWfiopCGxTfTKFNy?usp=sharing)
 
 * We use ImageDataGenerator to rescale the image data into float values (divide by 255 so the tensor values are between 0 and 1), and call flow_from_directory() to create two generators: one for the training dataset and one for the validation dataset.
 
 * On each iteration, these generators provide a batch of images by reading images from disk and processing them to the proper tensor size (224 x 224). The output is a tuple of (images, labels).
  
# Build the model

We'll create a model that's capable of transfer learning on just the last fully-connected layer.

We'll start with [MobileNet V2](https://www.tensorflow.org/api_docs/python/tf/keras/applications/mobilenet_v2) from Keras as the base model, which is pre-trained with the ImageNet dataset (trained to recognize 1,000 classes). This provides us a great feature extractor for image classification and we can then train a new classification layer with our  dataset. 

**note** : Paper [MobileNetV2: Inverted Residuals and Linear Bottlenecks](https://arxiv.org/abs/1801.04381)

# Create the base model

When instantiating the [MobileNet V2](https://www.tensorflow.org/api_docs/python/tf/keras/applications/mobilenet_v2), we set trainable false to freeze all the weights in the base model. This action should be done because we don't want model to using the weight that it already learn, also it can reduce time and prevent overfitting.

# Add a costumization layers
For the better accuracy and reduce loss we decided to use this costumization layers below:
![Screenshot 2024-06-21 013157](https://github.com/wahyuardiantito/DermaFace-An-App-For-Your-Facial-Skin-and-Care/assets/102838149/450dbf1d-e035-4654-8969-8d001fb15d97)

# Configure the model and fine tune :
for the fine tune and configure, we use Adam for the optimiser with the default learning rate (0.001). We also use Categorical Crossentropy for the loss function. After that we compile the model using .compile() function. This step is needed so we can train the model with our dataset

# Train the model

Now we can train the model using data provided by the train_generator and val_generator that we created at the beginning.
![image](https://github.com/wahyuardiantito/DermaFace-An-App-For-Your-Facial-Skin-and-Care/assets/102838149/c981f51b-73c0-47ef-9b4f-0a76e8167dd2)
![Screenshot 2024-06-20 233029](https://github.com/wahyuardiantito/DermaFace-An-App-For-Your-Facial-Skin-and-Care/assets/102838149/1f2643ac-d41e-4772-9615-a778dbd003cd)
![Screenshot 2024-06-20 233002](https://github.com/wahyuardiantito/DermaFace-An-App-For-Your-Facial-Skin-and-Care/assets/102838149/9957f13d-1930-42dc-aee7-7ce2c089a8db)

Our model better, but it's not ideal.

The validation loss is still higher than the training loss, so there could be some overfitting during training. The overfitting might also be because the new training set is relatively small with less intra-class variance, compared to the original ImageNet dataset used to train [MobileNet V2](https://www.tensorflow.org/api_docs/python/tf/keras/applications/mobilenet_v2).

# Convert Model to TFLite
Ordinarily, creating a TensorFlow Lite model is just a few lines of code with [TFLiteConverter](https://www.tensorflow.org/api_docs/python/tf/lite/TFLiteConverter). 

To fully quantize the model, we need to perform [post-training quantization](https://www.tensorflow.org/lite/performance/post_training_quantization) with a representative dataset, which requires a few more arguments for the TFLiteConverter, and a function that builds a dataset that's representative of the training dataset.
