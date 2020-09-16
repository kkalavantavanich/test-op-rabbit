package main

import akka.actor.{ActorSystem, Kill, Props}
import com.rabbitmq.client.Address
import com.spingo.op_rabbit._

import scala.concurrent.duration.{FiniteDuration, SECONDS}

object Main {

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val actorSystem: ActorSystem = ActorSystem("such-system")
    implicit val recoveryStrategy = RecoveryStrategy.none

    // First, initialize with wrong password
    val wrongConnectionParams = ConnectionParams(
      hosts = Seq(new Address("localhost")),
      username = "guest",
      password = "",
      virtualHost = "/",
      ssl = false
    )
    val wrongRabbitControl = actorSystem.actorOf(Props {
      new RabbitControl(wrongConnectionParams)
    })

    val wrongSubscriptionRef = Subscription.run(wrongRabbitControl) {
      import com.spingo.op_rabbit.Directives._
      channel(qos = 3) {
        consume(topic(queue("such-message-queue"), List("some-topic.#"))) {
          (body(as[String]) & routingKey) { (text, key) =>
            println(s"[Wrong] Received: '$text' over key '$key'")
            ack
          }
        }
      }
    }

    wrongSubscriptionRef.initialized.foreach { _ =>
      println("[Wrong] Initialized!")
      wrongRabbitControl ! Message.topic("Message from Wrong", "some-topic.cool")
    }

    // After 25 seconds, password is updated and triggers reload
    Thread.sleep(25000)
    println("New password received!")
    val correctConnectionParams = wrongConnectionParams.copy(password = "guest")

    // SubscriptionRef was never closed
    wrongSubscriptionRef.close(FiniteDuration(20, SECONDS))

    val correctRabbitControl = actorSystem.actorOf(Props {
      new RabbitControl(correctConnectionParams)
    })

    val correctSubscriptionRef = Subscription.run(correctRabbitControl) {
      import com.spingo.op_rabbit.Directives._
      channel(qos = 3) {
        consume(topic(queue("such-message-queue"), List("some-topic.#"))) {
          (body(as[String]) & routingKey) { (text, key) =>
            println(s"[Correct] Received: '$text' over key '$key'")
            ack
          }
        }
      }
    }

    correctSubscriptionRef.initialized.foreach { _ =>
      println("[Correct] Initialized!")
      correctRabbitControl ! Message.topic("Message from Correct", "some-topic.cool")
    }

    // After 30 seconds, let's close the application
    Thread.sleep(30000)
    correctSubscriptionRef.close(FiniteDuration(20, SECONDS))

    // Wait for correct subscription ref to close
    Thread.sleep(20000)

    actorSystem.terminate()
    println("Done!")
  }

}
