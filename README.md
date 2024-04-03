# vlasniydodatok
# Власний додаток
<b style="color:red;"> ДЛЯ ТОГО ЩОБ ЦЕЙ ДОДАТОК РОБИВ ПОТРІБНО МАТИ КОНТРОЛЛЕР ТА REST </b>

## REST API
Використовується для отримання еффектів з бази данних

```java
// Частина коду

  @Async
    @GetMapping("/getalleffects")
    @ResponseBody
    public CompletableFuture<ArrayList<Effect>> getTest() {
        System.out.println("Executing getalleffects method in thread: " + Thread.currentThread().getName());
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://sovyshka:???@cluster0.rpsi7sd.mongodb.net/?retryWrites=true&w=majority"));
        DB dataBase = mongoClient.getDB("leddb");
        DBCollection dataBaseCollection = dataBase.getCollection("visualeffects");

        BasicDBObject query = new BasicDBObject("effect", new BasicDBObject("$exists", true));
        DBCursor cursor = dataBaseCollection.find(query);

        ArrayList<Effect> effects = new ArrayList<>();

        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            String effectName = (String) document.get("name");
            String effectMode = (String) document.get("effect");
            int popularity = (int) document.get("popularity");
            String gif = (String) document.get("image");

            Effect effect = new Effect(effectName, effectMode, popularity, gif);
            effects.add(effect);
        }

        cursor.close();
        mongoClient.close();
        return CompletableFuture.completedFuture(effects);
    }

```

## Мобільний додаток
Використовується для посилання еффекту на контроллер

```java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new FetchEffectsTask().execute();
    }

    private class NetworkTask extends AsyncTask<JSONObject, Void, Void> {
        @Override
        protected Void doInBackground(JSONObject... jsonObjects) {
            try {
                sendJsonToServer(jsonObjects[0]);
                callApi(jsonObjects[0]);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void sendJsonToServer(JSONObject jsonObject) throws IOException {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(jsonObject.toString());
        out.close();
        socket.close();
        Log.d("responseCode", jsonObject.toString());
    }

    private void callApi(JSONObject jsonObject) throws IOException, JSONException {
        String apiUrl = "http://"+ REST_IP +":8080/api/increasepopularity/" + jsonObject.get("effect");
        Log.d("responseCode", apiUrl);
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        Log.d("responseCode", String.valueOf(connection.getResponseCode()));
    }
```


## Демонстрація роботи додатку
