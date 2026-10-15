Our real-life example will be an online pizza ordering system. The kitchen prepares order updates, customers subscribe to those updates, and customers can control how many updates they want to receive.

## 1. First, understand the real-life problem

Imagine an online pizza shop.

The shop needs to communicate order updates such as:

- Order received.
- Pizza is being prepared.
- Pizza is in the oven.
- Pizza is out for delivery.
- Order completed.

Here is the problem: what if the kitchen produces updates faster than the customer can process them? Or what if the customer no longer wants updates?

We need a system in which:

- The Publisher sends updates.
- The Subscriber receives updates.
- The Subscription manages the relationship between them.

This is the purpose of the three interfaces in your image.

## 2. Understand the three interfaces

1\. Publisher — the sender

The pizza shop's order-update system. It allows a subscriber to register for updates.

Java method: `subscribe(...)`

2\. Subscriber — the receiver

The customer's app, which receives order updates and reacts to them.

Methods: `onSubscribe`, `onNext`, `onError`, `onComplete`

3\. Subscription — the controller

The connection between the customer and the shop. It lets the customer request a certain number of updates or cancel the connection.

Methods: `request(...)`, `cancel()`

The important idea is that the subscriber can control the flow of data. This is called backpressure.

For example, a customer might initially request two updates. The publisher should not send a third update until the customer requests more.

## 3. Step one — implement the `Subscription` interface

Now we need to define how the customer controls the updates.

In real life, when you order pizza, you might say:

- “Send me the next two updates.”
- “Stop sending me updates.”

Our `Subscription` interface provides exactly these two operations.

```
public interface Subscription {
    void request(long n);
    void cancel();
}
```

### What does each method mean?

- `request(long n)` — asks the publisher to send up to `n` more items.
- `cancel()` — tells the publisher to stop sending items through this subscription.

For example:

```
subscription.request(2);
subscription.cancel();
```

The first line requests two updates. The second line cancels the subscription.

Important: `request(2)` means two additional items, not two items in total for the entire lifetime of the subscription.

### Now we need to implement this interface

An interface describes what an object must do. It does not implement the behavior itself.

So, let's create a class called `PizzaSubscription`.

```
public class PizzaSubscription implements Subscription {

    @Override
    public void request(long n) {
        System.out.println("Customer requested " + n + " updates");
    }

    @Override
    public void cancel() {
        System.out.println("Customer cancelled updates");
    }
}
```

This is our first implementation, but it only prints messages. It doesn't actually deliver pizza updates yet.

We'll connect it to the publisher shortly.

## 4. Step two — implement the `Subscriber` interface

Now we need someone to receive the updates.

In our example, the customer's mobile app is the subscriber.

The interface in your image is:

```
public interface Subscriber<T> {

    void onSubscribe(Subscription s);

    void onNext(T t);

    void onError(Throwable t);

    void onComplete();
}
```

Let's understand each method using our pizza shop.

| Method           | Real-life meaning                                             |
| ---------------- | ------------------------------------------------------------- |
| `onSubscribe(s)` | The shop establishes a connection with the customer's app.    |
| `onNext(t)`      | The app receives an order update.                             |
| `onError(t)`     | Something goes wrong while processing the stream.             |
| `onComplete()`   | No more updates will be sent because the stream has finished. |

Notice the `<T>` in `Subscriber<T>`. This is a generic type.

If we write `Subscriber<String>`, each update is a string. If we write `Subscriber<Integer>`, each item is an integer.

### Now let's implement the customer's app

```
public class PizzaOrderSubscriber
        implements Subscriber<String> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;

        System.out.println("Connected to pizza shop!");

        // Request the first two updates.
        subscription.request(2);
    }

    @Override
    public void onNext(String update) {
        System.out.println("Pizza update: " + update);
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("All order updates completed.");
    }
}
```

### What did we just do?

1. We created `PizzaOrderSubscriber`.
2. When the publisher connects, `onSubscribe()` receives the subscription.
3. We save the subscription so the customer can control it later.
4. We request two updates.
5. Each received update is printed using `onNext()`.
6. If an error happens, `onError()` handles it.
7. When all updates finish, `onComplete()` is called.

Why save the subscription in a field?

Because the customer may want to request more updates later or cancel them. Without saving the reference, the subscriber would have no convenient way to control its subscription after the initial callback.

## 5. Step three — implement the `Publisher` interface

Now we need the pizza shop to send updates.

The interface from your image is:

```
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> s);
}
```

The publisher is responsible for accepting a subscriber and establishing a subscription.

Let's implement a simple publisher that sends updates from a list.

```
import java.util.List;

public class PizzaOrderPublisher
        implements Publisher<String> {

    private final List<String> updates;

    public PizzaOrderPublisher(List<String> updates) {
        this.updates = List.copyOf(updates);
    }

    @Override
    public void subscribe(Subscriber<? super String> subscriber) {

        Subscription subscription = new Subscription() {
            private int index = 0;
            private boolean cancelled = false;
            private boolean completed = false;

            @Override
            public void request(long n) {
                if (cancelled || completed) {
                    return;
                }

                if (n <= 0) {
                    cancelled = true;
                    subscriber.onError(
                        new IllegalArgumentException(
                            "Request must be positive"
                        )
                    );
                    return;
                }

                long sent = 0;

                while (sent < n
                        && index < updates.size()
                        && !cancelled) {

                    String update = updates.get(index++);
                    subscriber.onNext(update);
                    sent++;
                }

                if (!cancelled && index == updates.size()) {
                    completed = true;
                    subscriber.onComplete();
                }
            }

            @Override
            public void cancel() {
                cancelled = true;
                System.out.println("Subscription cancelled.");
            }
        };

        subscriber.onSubscribe(subscription);
    }
}
```

### Let's understand what we implemented

First, we store the updates.

```
private final List<String> updates;
```

The list represents all the order updates the shop can publish.

Next, we accept a subscriber.

```
public void subscribe(Subscriber<? super String> subscriber)
```

The customer registers with the shop. The `? super String` wildcard allows a subscriber of `String` or a suitable supertype, such as `Object`.

Next, we create a subscription.

```
Subscription subscription = new Subscription() {
    // Implementation
};
```

This is an anonymous class: we implement `Subscription` directly inside `subscribe()`. That subscription tracks the number of updates already sent and whether the customer cancelled.

Next, we respect demand.

```
while (sent < n && index < updates.size() && !cancelled)
```

This loop sends no more than the number of items requested by the subscriber during that request.

Finally, we establish the connection.

```
subscriber.onSubscribe(subscription);
```

The publisher gives the subscriber its subscription. The subscriber can then call `request()` or `cancel()`.

A technical note: this is an educational, synchronous implementation of the basic protocol. A production-quality Reactive Streams publisher must also handle concurrent and reentrant requests, serialization of signals, and other protocol requirements. In particular, this example isn't a complete, production-ready Reactive Streams implementation.

## 6. Step four — allow the customer to request more updates

Right now, our subscriber requests only two updates when it first connects.

Now we need to give the customer a way to request additional updates.

Add these methods to `PizzaOrderSubscriber`, inside the class:

```
public void requestMore(long n) {
    subscription.request(n);
}

public void stopUpdates() {
    subscription.cancel();
}
```

Now our customer can decide what happens next.

For example:

```
PizzaOrderSubscriber customer = new PizzaOrderSubscriber();

customer.requestMore(2);  // Request two additional updates
customer.stopUpdates();   // Stop receiving updates
```

In the complete example below, we'll request more updates until the order stream finishes. We'll also see how cancellation works.

## 7. Step five — connect everything in `main()`

Now we need to bring all three interfaces together.

We need three things:

1. A publisher containing the pizza order updates.
2. A subscriber representing the customer's app.
3. A subscription created by the publisher when the customer subscribes.

Here's our complete application.

```

import java.util.List;

// 1. Define the Subscription interface
interface Subscription {
    void request(long n);
    void cancel();
}

// 2. Define the Subscriber interface
interface Subscriber<T> {
    void onSubscribe(Subscription s);
    void onNext(T item);
    void onError(Throwable error);
    void onComplete();
}

// 3. Define the Publisher interface
interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}

// 4. Implement the publisher
class PizzaOrderPublisher implements Publisher<String> {

    private final List<String> updates;

    PizzaOrderPublisher(List<String> updates) {
        this.updates = List.copyOf(updates);
    }

    @Override
    public void subscribe(Subscriber<? super String> subscriber) {

        Subscription subscription = new Subscription() {
            private int index = 0;
            private boolean cancelled = false;
            private boolean completed = false;

            @Override
            public void request(long n) {
                if (cancelled || completed) {
                    return;
                }

                if (n <= 0) {
                    cancelled = true;
                    subscriber.onError(
                        new IllegalArgumentException(
                            "Request must be positive"
                        )
                    );
                    return;
                }

                long sent = 0;

                while (sent < n
                        && index < updates.size()
                        && !cancelled) {
                    subscriber.onNext(updates.get(index++));
                    sent++;
                }

                if (!cancelled && index == updates.size()) {
                    completed = true;
                    subscriber.onComplete();
                }
            }

            @Override
            public void cancel() {
                cancelled = true;
                System.out.println("Updates cancelled.");
            }
        };

        subscriber.onSubscribe(subscription);
    }
}

// 5. Implement the subscriber
class PizzaOrderSubscriber implements Subscriber<String> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
        System.out.println("Customer connected!");
        subscription.request(2);
    }

    @Override
    public void onNext(String update) {
        System.out.println("Pizza update: " + update);
    }

    @Override
    public void onError(Throwable error) {
        System.out.println("Error: " + error.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("Order update stream completed.");
    }

    public void requestMore(long n) {
        subscription.request(n);
    }

    public void stopUpdates() {
        subscription.cancel();
    }
}

// 6. Run the application
public class Main {
    public static void main(String[] args) {

        PizzaOrderPublisher shop = new PizzaOrderPublisher(
            List.of(
                "Order received",
                "Pizza is being prepared",
                "Pizza is in the oven",
                "Pizza is out for delivery",
                "Pizza delivered"
            )
        );

        PizzaOrderSubscriber customer =
            new PizzaOrderSubscriber();

        // The customer subscribes to the shop.
        shop.subscribe(customer);

        // Request two additional updates.
        customer.requestMore(2);

        // Request the remaining updates.
        customer.requestMore(2);
    }
}

```

Save the code as `Main.java`. Because this example defines all three interfaces in the same file, you can compile and run it with a standard JDK:

```
javac Main.java
java Main
```

This example defines simplified versions of the interfaces shown in your image, so it doesn't require an external Reactive Streams library.

## 8. Step six — understand the output

When the program runs, the output will be:

```
Customer connected!
Pizza update: Order received
Pizza update: Pizza is being prepared
Pizza update: Pizza is in the oven
Pizza update: Pizza is out for delivery
Pizza update: Pizza delivered
Order update stream completed.
```

Let's trace exactly what happens.

## 1

The customer subscribes

`shop.subscribe(customer)`

The publisher creates a subscription and calls `customer.onSubscribe(subscription)`.

## 2

The first two updates are sent

`subscription.request(2)`

The publisher sends “Order received” and “Pizza is being prepared.”

## 3

The customer requests two more

`customer.requestMore(2)`

The publisher sends “Pizza is in the oven” and “Pizza is out for delivery.”

## 4

The final request completes the stream

`customer.requestMore(2)`

Only one update remains: “Pizza delivered.” The publisher sends it, then calls `onComplete()`.

Notice that the final request asks for two items, but only one remains. The publisher sends that one item and completes the stream. It doesn't invent a sixth update to fill the request.

## 9. What happens if the customer cancels?

Let's use the same real-life example.

Imagine the customer no longer wants notifications because they are watching the delivery driver on a map.

Now we need to cancel the subscription.

```
shop.subscribe(customer);

// Customer requests the first two updates automatically.

// The customer no longer wants updates.
customer.stopUpdates();
```

When `stopUpdates()` calls `subscription.cancel()`, the publisher marks that subscription as cancelled. Its request loop checks this flag and stops sending further items.

In our current implementation, the cancellation message will be printed, and later requests won't deliver more updates.

One important detail: cancellation stops future deliveries; it doesn't undo updates already received.

## 10. The complete relationship between the three interfaces

Publisher

Pizza shop — `subscribe()`

Creates a Subscription and calls `onSubscribe()`

Subscription

Controls demand with `request(n)` and `cancel()`

Delivers requested items through `onNext()`

Subscriber

Customer app — receives and processes updates

When the stream finishes

The publisher calls `onComplete()`, or `onError()` if a terminal error occurs.

## 11. What you should remember

| Interface       | Main responsibility                                     | Pizza example                    |
| --------------- | ------------------------------------------------------- | -------------------------------- |
| `Publisher<T>`  | Accepts subscribers and starts the subscription process | Pizza shop                       |
| `Subscriber<T>` | Receives and processes events                           | Customer's app                   |
| `Subscription`  | Controls demand and cancellation                        | Customer's notification controls |

One last important point: in a real Reactive Streams project, you normally use the official interfaces rather than defining your own. For example, the interfaces are available through the `org.reactivestreams` package and are used by libraries such as Project Reactor and RxJava. Our example is designed to teach you the underlying mechanics, not to replace a production implementation.

The mental model to keep in mind is simple:

- The publisher says, “I have data.”
- The subscriber says, “I'm ready to receive data.”
- The subscription controls, “How much data can I receive, and when do I stop?”

Once you understand these three responsibilities, you'll find it much easier to understand reactive programming in Java.