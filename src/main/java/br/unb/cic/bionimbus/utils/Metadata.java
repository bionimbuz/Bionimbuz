package br.unb.cic.bionimbus.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import br.unb.cic.bionimbus.services.storage.FileMetadata;

public class Metadata {

	public static void criarMetadata(String path, String host, String usuario, int versao) throws NoSuchAlgorithmException, IOException {
		
		Metadata md = new Metadata();
		
		File f = new File(path);
		FileMetadata fm = new FileMetadata();
		fm.setNome(f.getName());
		fm.setDataCriacao(f.lastModified());
		String extensao = f.getName().substring(f.getName().lastIndexOf(".")+1);
		fm.setFormato(extensao);
		fm.setHost(host);
		fm.setSha1(md.calcularSha1(f));
		fm.setSize(f.length());
		fm.setUsuario(usuario);
		fm.setVersao(versao);

		String fileName = path + ".xml"; 
		md.marshalToFile(fm, fileName);
		
	}
	
	public String calcularSha1(File file) throws NoSuchAlgorithmException, IOException {
		 MessageDigest md = MessageDigest.getInstance("SHA-1");  
		 FileInputStream fis = new FileInputStream(file);
		 byte[] buf = new byte[1024];
		 int read;
		 while ((read = fis.read(buf, 0, 1024)) != -1 ) {
			 md.update(buf, 0, read);
		 }
		 fis.close();
		 
		 //byte[] fp = md.digest();		 
		 BigInteger hash = new BigInteger(1, md.digest());

		 return hash.toString(16);
	}
	
    /**
     * Converte o objeto em uma String com estrutura XML.
     * @param object objeto a ser convertido em XML.
     * @return String contendo a estrutura XML.
     */
    public String marshal(Object object) {
        final StringWriter out = new StringWriter();
        JAXBContext context = null;
        Marshaller marshaller = null;
        try {
            context = JAXBContext.newInstance(object.getClass());
            marshaller = context.createMarshaller();
            marshaller.setProperty(
                    javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE
            );
            marshaller.marshal(object, new StreamResult(out));
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return out.toString();
    }
	
		/**
	    * Converte o objeto em uma estrutura XML.
	    * @param object objeto a ser convertido em XML.
	    * @param fileName nome do arquivo XML a ser gerado.
	    * @return uma string com o conteudo do XML gerado.
	    */
	   public String marshalToFile(Object object, String fileName) {
	       final StringWriter out = new StringWriter();
	       JAXBContext context = null;
	       Marshaller marshaller = null;
	       try {
	           context = JAXBContext.newInstance(object.getClass());
	           marshaller = context.createMarshaller();
	           marshaller.setProperty(
	                   javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
	                   Boolean.TRUE
	           );
	           marshaller.marshal(object, new StreamResult(out));
	       } catch (PropertyException e) {
	           e.printStackTrace();
	       } catch (JAXBException e) {
	           e.printStackTrace();
	       }
	 
	       Writer writer = null;
	       try {
	           writer = new FileWriter(fileName);
	           marshaller.marshal(object, writer);
	       } catch (JAXBException e) {
	           e.printStackTrace();
	       } catch (IOException e) {
	           e.printStackTrace();
	       } finally {
	           try {
	               if (writer != null) {
	                   writer.close();
	               }
	           } catch (Exception e) {
	               e.getMessage();
	           }
	       }
	 
	       return out.toString();
	   }

	   /**
	     * Converte um string com estrutura XML em um objeto.
	     * @param clazz classe referente ao tipo do objeto a ser retornado.
	     * @param stringXml string com o conteudo XML a ser convertido em objeto.
	     * @return retorna um novo objeto de clazz.
	     */
	    public Object unmarshal(Class clazz, String stringXml) {
	        JAXBContext context = null;
	        Unmarshaller unmarshaller = null;
	        try {
	            context = JAXBContext.newInstance(clazz);
	            unmarshaller = context.createUnmarshaller();
	            return unmarshaller.unmarshal(
	                    new StreamSource(new StringReader(stringXml))
	            );
	        } catch (JAXBException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	    
	    /**
	     * Realiza a conversao (unmarshal) de um arquivo XML em um objeto do seu tipo.
	     * @param clazz classe referente ao objeto a ser criado a partir do XML.
	     * @param fileXml nome do arquivo XML a ser convertido em objeto.
	     * @return novo objeto.
	     */
	    public Object unmarshalFromFile(Class clazz, String fileXml) {
	        JAXBContext context = null;
	        Unmarshaller unmarshaller = null;
	        try {
	            context = JAXBContext.newInstance(clazz);
	            unmarshaller = context.createUnmarshaller();
	            return unmarshaller.unmarshal(
	                    new FileInputStream(fileXml)
	            );
	        } catch (JAXBException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
}
