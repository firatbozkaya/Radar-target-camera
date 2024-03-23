package BusinessLogicLayer;

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

public class camera_control {

    // Variables
    private Positions positions;
    private Config config;
    private Consumer<String, String> consumer;
    private Producer<String, String> producer;
    private ScheduledExecutorService scheduler;
    private Helper helper;
    private Calculator calculator;
    private double angle;
    private double distance;
    private double cameraAngle;
    // Variables

    public camera_control(){
        initParameters();
    }

    private void initParameters(){
        config = Config.getInstance();
        positions = Positions.getInstance();
        scheduler = Executors.newScheduledThreadPool(1);
        producer = createProducer();
        consumer = createConsumer();
        helper = new Helper();
        calculator = new Calculator();
    }

    private Producer<String, String> createProducer(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "camera-control-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    private Consumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "camera-control-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("TargetBearingPosition"));
        return consumer;
    }

    public void startListenTargetBearingPosition() {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(config.getSensorListenmHZ()));
        for (ConsumerRecord<String, String> record : records) {
            JSONObject jsonObject;
            try {
                jsonObject = helper.returnJSONObjectByString(record.value());
                angle = Double.parseDouble(jsonObject.get("angle").toString());
                distance = Double.parseDouble(jsonObject.get("distance").toString());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            calculateAngle();
        }
    }

    private void calculateAngle(){
        cameraAngle = calculator.calculateAngleAndDistance(
                positions.getRadarXPosition(),
                helper.getItemYPosition(config.getJFrameHeight(),positions.getRadarYPosition()),
                positions.getCameraXPosition(),
                helper.getItemYPosition(config.getJFrameHeight(),positions.getCameraYPosition()),
                angle,
                distance
        );
        positions.setCameraAngle(cameraAngle);
    }

    public void startProducer(){
        scheduler.scheduleAtFixedRate(this::publishCameraLosStatus, 0, config.getSensorPublishmHZ(), TimeUnit.MILLISECONDS);
    }

    public void stopProducer() {
        scheduler.shutdown();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public void resumeProducer() {
        scheduler.scheduleAtFixedRate(this::publishCameraLosStatus, 0, config.getSensorPublishmHZ(), TimeUnit.MILLISECONDS);
    }

    private void publishCameraLosStatus() {
        String targetMessage = helper.createStringByJson(
                "cameraAngle",String.valueOf(cameraAngle)
        );
        producer.send(new ProducerRecord<>("CameraLosStatus", "CameraLosStatus", targetMessage));
    }



}
