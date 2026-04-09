# German → English Translation LLM

# 1. Install Dependencies
# pip install transformers datasets sacrebleu accelerate sentencepiece fastapi uvicorn sacremoses

from datasets import load_dataset

# Load dataset
raw_dataset = load_dataset("opus_books", "de-en")

# Create train/test split
raw_split = raw_dataset["train"].train_test_split(test_size=0.1)

# Load Tokenizer + Model
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

model_name = "Helsinki-NLP/opus-mt-de-en"

tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSeq2SeqLM.from_pretrained(model_name)

# Preprocessing
max_length = 64

def preprocess(example):
    inputs = [x["de"] for x in example["translation"]]
    targets = [x["en"] for x in example["translation"]]

    model_inputs = tokenizer(
        inputs,
        max_length=max_length,
        truncation=True,
        padding="max_length"
    )

    labels = tokenizer(
        text_target=targets,
        max_length=max_length,
        truncation=True,
        padding="max_length"
    )

    model_inputs["labels"] = labels["input_ids"]
    return model_inputs

# Tokenize post-splitting
tokenized_dataset = raw_split.map(preprocess, batched=True)

# Reduce Dataset Size
small_train = tokenized_dataset["train"].select(range(5000))
small_test = tokenized_dataset["test"].select(range(1000))

# Train Model
from transformers import TrainingArguments, Trainer

training_args = TrainingArguments(
    output_dir="./results",
    learning_rate=5e-5,
    per_device_train_batch_size=4,
    per_device_eval_batch_size=4,
    num_train_epochs=1,
    weight_decay=0.01,
    logging_steps=100
)

trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=small_train,
    eval_dataset=small_test
)

trainer.train()

# Evaluate Model (BLEU)
import sacrebleu

def evaluate_model(samples=100):
    predictions = []
    references = []

    for i in range(samples):
        example = raw_split["test"][i]

        input_text = example["translation"]["de"]
        reference = example["translation"]["en"]

        inputs = tokenizer(input_text, return_tensors="pt", truncation=True)
        outputs = model.generate(**inputs, max_length=64)

        pred = tokenizer.decode(outputs[0], skip_special_tokens=True)

        predictions.append(pred)
        references.append([reference])

    bleu = sacrebleu.corpus_bleu(predictions, references)
    print("BLEU Score:", bleu.score)

# Run evaluation
evaluate_model()

# Inference Function
def translate(text):
    inputs = tokenizer(text, return_tensors="pt", truncation=True)
    outputs = model.generate(**inputs, max_length=64)
    return tokenizer.decode(outputs[0], skip_special_tokens=True)

# Example
print(translate("Ich liebe Programmierung."))

# Possible Improvements
# - Replace dataset with WMT for better quality
# - Use LoRA for efficient fine-tuning
# - Increase max_length for longer sentences
# - Train for more epochs
# - Use GPU for faster training (Mine is stinky)