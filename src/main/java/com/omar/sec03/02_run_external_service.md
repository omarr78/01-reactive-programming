You can run the `external-services.jar` using Docker without installing Java directly on your machine. Since the Dockerfile is already provided, let's do it step by step on Windows.

## 1. Create a Dockerfile

Create a folder, for example `external-services`, and inside it create a file named exactly `Dockerfile` (without an extension).

Put this code inside it:

```

FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

ADD https://github.com/vinsguru/java-reactive-programming-course/raw/master/02-external-services/external-services.jar external-services.jar

CMD ["java", "-jar", "external-services.jar"]

```

What does this do?

- `FROM`: Uses an image containing Java 21.
- `WORKDIR`: Sets `/app` as the working directory inside the container.
- `ADD`: Downloads the JAR file into the image during the build.
- `CMD`: Runs the JAR when the container starts.

## 2. Build the Docker image

Open a terminal in the folder containing your Dockerfile and execute:

```
docker build -t external-services .
```

Explanation:

- `docker build`: Builds an image.
- `-t external-services`: Gives your image the name `external-services`.
- `.`: Uses the current folder as the build context.

Wait until the build finishes successfully.

## 3. Run the container

Execute:

```
docker run -d --name external-services -p 7070:7070 external-services
```

