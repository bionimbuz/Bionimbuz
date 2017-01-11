package br.unb.cic.bionimbuz.services.tarification.Utils.RestfulGetterBehaviors;

/**
 *
 * @author Gabriel Fritz Sluzala
 * implements RestfulGetter 
 */
public class PricingGet {

//    public PricingGet() {
//    }
//
//    @Override
//    public String get(String server, String address) {
//
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//
//        try {
//
//            // Especifica o host, a porta e o protocolo
//            HttpHost target = new HttpHost(server, 80, "http");
//            // Especifica o get request
//            HttpGet getRequest = new HttpGet(address);
//
//            /*Important stuff...*/
//            System.out.println("executing request to " + target);
//
//            HttpResponse httpResponse = httpClient.execute(target, getRequest);
//            HttpEntity entity = httpResponse.getEntity();
//
//            System.out.println("----------------------------------------");
//            System.out.println(httpResponse.getStatusLine());
//            Header[] headers = httpResponse.getAllHeaders();
//
//            for (Header header : headers) {
//                System.out.println(header);
//            }
//
//            System.out.println("----------------------------------------");
//            /*End of important stuff*/
//
// /*Caso o resultado do request não seja nulo, ele é tratado*/
//            if (entity != null) {
//                String ent =EntityUtils.toString(entity);
//                ArrayList<String> j = new ArrayList(Arrays.asList(ent.split("},")));
//                ArrayList<String> j2 = new ArrayList();
//                Set<String> hs = new LinkedHashSet<>();
//                int aux = 1;
//                for (String j1 : j) {
//                    if (aux++ != j.size()) 
//                        j1= j1.concat("}");
//                    j2.add(j1);
//                }
//                hs.addAll(j2);
//                j2.clear();
//                j.clear();
//                String computeEngineAux = hs.toString();
//                computeEngineAux= computeEngineAux.substring(1, computeEngineAux.length()-1);
//                computeEngineAux=computeEngineAux.replaceAll("}, ", "},");
//                return computeEngineAux;
//            } else {
//                return null;
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                httpClient.close();
//            } catch (Exception ex) {
//                Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return null;
//    }
//
//    /**
//     *
//     * @param array
//     * @param filename
//     */
//    @Override
//    public void saveGet(String array, String filename) {
//
//        try {
//            OutputStream os = new FileOutputStream(filename);
//            OutputStreamWriter osw = new OutputStreamWriter(os);
//            try (BufferedWriter bw = new BufferedWriter(osw)) {
//                bw.write(array);
//            } catch (IOException ex) {
//                Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(PricingGet.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
