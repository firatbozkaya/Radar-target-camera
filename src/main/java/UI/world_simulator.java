package UI;

import BusinessLogicLayer.Helper;
import Entities.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class world_simulator {

    // Variables
    private Positions positions;
    private Config config;
    private Helper helper;
    private ScheduledExecutorService scheduler;
    private Producer<String, String> producer;
    private Consumer<String, String> consumer;
    // Variables

    public world_simulator(){
        initParameters();
    }

    private void initParameters(){
        helper = new Helper();
        config = Config.getInstance();
        positions = Positions.getInstance();
        scheduler = Executors.newScheduledThreadPool(1);
        producer = createProducer();
        consumer = createConsumer();
    }

    private Producer<String, String> createProducer(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "world-simulator-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    private static Consumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "world-simulator-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("CameraLosStatus"));
        return consumer;
    }

    public void startListenConsumerCameraLosStatus() {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(config.getSensorListenmHZ()));
        for (ConsumerRecord<String, String> record : records) {
            JSONObject jsonObject;
            try {
                jsonObject = helper.returnJSONObjectByString(record.value());
                positions.setCameraLosStatusDegree(
                        Double.parseDouble(jsonObject.get("cameraAngle").toString())
                );
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            System.out.println("ListeningCameraLosStatus message: " + positions.getCameraLosStatusDegree());
        }
    }

    public void startProducer() {
        scheduler.scheduleAtFixedRate(
                this::publishTargetPointPositionAndTargetPointPosition, 0, config.getSensorPublishmHZ(), TimeUnit.MILLISECONDS);
    }

    public void stopProducer() {
        scheduler.shutdown();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void resumeProducer() {
        scheduler.scheduleAtFixedRate(
                this::publishTargetPointPositionAndTargetPointPosition, 0, config.getSensorPublishmHZ(), TimeUnit.MILLISECONDS);
    }

    private void publishTargetPointPositionAndTargetPointPosition() {
        String targetMessage = helper.createStringByJson(
                "targetXPosition",String.valueOf(positions.getPlaneXPosition()),
                "targetYPosition",String.valueOf(positions.getPlaneYPosition())
        );
        producer.send(new ProducerRecord<>("TargetPointPosition", "TargetPointPosition", targetMessage));

        String towerAndCameraMessage = helper.createStringByJson(
                "radarXPosition",String.valueOf(positions.getRadarXPosition()),
                "radarYPosition",String.valueOf(positions.getRadarYPosition()),
                "cameraXPosition",String.valueOf(positions.getCameraXPosition()),
                "cameraYPosition",String.valueOf(positions.getCameraYPosition())
        );
        producer.send(new ProducerRecord<>("TowerPosition", "TowerPosition", towerAndCameraMessage));
    }
}
