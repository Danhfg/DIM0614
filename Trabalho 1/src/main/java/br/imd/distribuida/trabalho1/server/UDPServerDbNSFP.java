package br.imd.distribuida.trabalho1.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.input.ReversedLinesFileReader;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.Predict;
import br.imd.distribuida.trabalho1.models.ServerResponse;

public class UDPServerDbNSFP {
	
	private Gson gson = new Gson();
	private Algorithm algorithm = Algorithm.HMAC256("AOsD89f&*Fujalo()*");

	public UDPServerDbNSFP() {
		int port = 8888;
		try {
			DatagramSocket serverSocket = new DatagramSocket(port);

			System.out.println("UDP Server DBNSFP Started " + port);
			while(true) {
				byte[] receiveMessage = new byte[5120];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				serverSocket.receive(receivePacket);

				String message = new String(receivePacket.getData());
				message = message.replaceAll("\u0000.*", "");
				if(message.contains("PING")){
					System.out.println(message);
					byte[] sendMessage;
					String send = "PONG";
					sendMessage = send.getBytes();
					
		            InetAddress addressClient = receivePacket.getAddress();
		            int portClient = receivePacket.getPort();
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							addressClient, portClient);
					serverSocket.send(sendPacket);

				}
				else {
				
					try {
						Predict pred = (Predict) gson.fromJson(message, Predict.class);
						
						//DecodedJWT jwt = JWT.decode(pred.getToken());
	
					    JWTVerifier verifier = JWT.require(algorithm)
					        .withIssuer("predictor")
					        .build(); //Reusable verifier instance
					    DecodedJWT jwt = verifier.verify(pred.getToken());
					    
						String e = jwt.getClaim("user").asString();
						System.out.println("User: " + e);
						System.out.println("Predict received. Chr: " + pred.getChr()+", pos: "+ pred.getPos()+ ", ref: " + pred.getRef()+", alt: " + pred.getAlt());
	
						FileWriter fileWriter = new FileWriter("data/"+e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+".vcf");
						fileWriter.write("##fileformat=VCFv4.0\r\n" + 
								"##fileDate="+ LocalDate.now() + "\r\n" + 
								"##patient="+ pred.getPatient() + "\r\n" + 
								"##source=dbSNP\r\n" + 
								"##dbSNP_BUILD_ID=132\r\n" + 
								"##reference=GRCh38\r\n" + 
								"##phasing=partial\r\n" + 
								"##variationPropertyDocumentationUrl=ftp://ftp.ncbi.nlm.nih.gov/snp/specs/dbSNP_BitField_latest.pdf\r\n" + 
								"##INFO=<ID=RV,Number=0,Type=Flag,Description=\"RS orientation is reversed\">\r\n" + 
								"##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">\r\n" + 
								"##INFO=<ID=AF,Number=.,Type=Float,Description=\"Allele Frequency\">\r\n" + 
								"##INFO=<ID=VP,Number=1,Type=String,Description=\"Variation Property\">\r\n" + 
								"##INFO=<ID=dbSNPBuildID,Number=1,Type=Integer,Description=\"First SNP Build for RS\">\r\n" + 
								"##INFO=<ID=WGT,Number=1,Type=Integer,Description=\"Weight, 00 - unmapped, 1 - weight 1, 2 - weight 2, 3 - weight 3 or more\">\r\n" + 
								"##INFO=<ID=VC,Number=1,Type=String,Description=\"Variation Class\">\r\n" + 
								"##INFO=<ID=CLN,Number=0,Type=Flag,Description=\"SNP is Clinical(LSDB,OMIM,TPA,Diagnostic)\">\r\n" + 
								"##INFO=<ID=PM,Number=0,Type=Flag,Description=\"SNP is Precious(Clinical,Pubmed Cited)\">\r\n" + 
								"##INFO=<ID=TPA,Number=0,Type=Flag,Description=\"Provisional Third Party Annotation(TPA) (currently rs from PHARMGKB who will give phenotype data)\">\r\n" + 
								"##INFO=<ID=PMC,Number=0,Type=Flag,Description=\"Links exist to PubMed Central article\">\r\n" + 
								"##INFO=<ID=S3D,Number=0,Type=Flag,Description=\"Has 3D structure - SNP3D table\">\r\n" + 
								"##INFO=<ID=SLO,Number=0,Type=Flag,Description=\"Has SubmitterLinkOut - From SNP->SubSNP->Batch.link_out\">\r\n" + 
								"##INFO=<ID=NSF,Number=0,Type=Flag,Description=\"Has non-synonymous frameshift A coding region variation where one allele in the set changes all downstream amino acids. FxnClass = 44\">\r\n" + 
								"##INFO=<ID=NSM,Number=0,Type=Flag,Description=\"Has non-synonymous missense A coding region variation where one allele in the set changes protein peptide. FxnClass = 42\">\r\n" + 
								"##INFO=<ID=NSN,Number=0,Type=Flag,Description=\"Has non-synonymous nonsense A coding region variation where one allele in the set changes to STOP codon (TER). FxnClass = 41\">\r\n" + 
								"##INFO=<ID=REF,Number=0,Type=Flag,Description=\"Has reference A coding region variation where one allele in the set is identical to the reference sequence. FxnCode = 8\">\r\n" + 
								"##INFO=<ID=SYN,Number=0,Type=Flag,Description=\"Has synonymous A coding region variation where one allele in the set does not change the encoded amino acid. FxnCode = 3\">\r\n" + 
								"##INFO=<ID=U3,Number=0,Type=Flag,Description=\"In 3' UTR Location is in an untranslated region (UTR). FxnCode = 53\">\r\n" + 
								"##INFO=<ID=U5,Number=0,Type=Flag,Description=\"In 5' UTR Location is in an untranslated region (UTR). FxnCode = 55\">\r\n" + 
								"##INFO=<ID=ASS,Number=0,Type=Flag,Description=\"In acceptor splice site FxnCode = 73\">\r\n" + 
								"##INFO=<ID=DSS,Number=0,Type=Flag,Description=\"In donor splice-site FxnCode = 75\">\r\n" + 
								"##INFO=<ID=INT,Number=0,Type=Flag,Description=\"In Intron FxnCode = 6\">\r\n" + 
								"##INFO=<ID=R3,Number=0,Type=Flag,Description=\"In 3' gene region FxnCode = 13\">\r\n" + 
								"##INFO=<ID=R5,Number=0,Type=Flag,Description=\"In 5' gene region FxnCode = 15\">\r\n" + 
								"##INFO=<ID=OTH,Number=0,Type=Flag,Description=\"Has other snp with exactly the same set of mapped positions on NCBI refernce assembly.\">\r\n" + 
								"##INFO=<ID=CFL,Number=0,Type=Flag,Description=\"Has Assembly conflict. This is for weight 1 and 2 snp that maps to different chromosomes on different assemblies.\">\r\n" + 
								"##INFO=<ID=ASP,Number=0,Type=Flag,Description=\"Is Assembly specific. This is set if the snp only maps to one assembly\">\r\n" + 
								"##INFO=<ID=MUT,Number=0,Type=Flag,Description=\"Is mutation (journal citation, explicit fact): a low frequency variation that is cited in journal and other reputable sources\">\r\n" + 
								"##INFO=<ID=VLD,Number=0,Type=Flag,Description=\"Is Validated.  This bit is set if the snp has 2+ minor allele count based on frequency or genotype data.\">\r\n" + 
								"##INFO=<ID=G5A,Number=0,Type=Flag,Description=\">5% minor allele frequency in each and all populations\">\r\n" + 
								"##INFO=<ID=G5,Number=0,Type=Flag,Description=\">5% minor allele frequency in 1+ populations\">\r\n" + 
								"##INFO=<ID=HD,Number=0,Type=Flag,Description=\"Marker is on high density genotyping kit (50K density or greater).  The snp may have phenotype associations present in dbGaP.\">\r\n" + 
								"##INFO=<ID=GNO,Number=0,Type=Flag,Description=\"Genotypes available. The snp has individual genotype (in SubInd table).\">\r\n" + 
								"##INFO=<ID=KGPilot1,Number=0,Type=Flag,Description=\"1000 Genome discovery(pilot1) 2009\">\r\n" + 
								"##INFO=<ID=KGPilot123,Number=0,Type=Flag,Description=\"1000 Genome discovery all pilots 2010(1,2,3)\">\r\n" + 
								"##INFO=<ID=KGVAL,Number=0,Type=Flag,Description=\"1000 Genome validated by second method\">\r\n" + 
								"##INFO=<ID=KGPROD,Number=0,Type=Flag,Description=\"1000 Genome production phase\">\r\n" + 
								"##INFO=<ID=PH1,Number=0,Type=Flag,Description=\"Phase 1 genotyped: filtered, non-redundant\">\r\n" + 
								"##INFO=<ID=PH2,Number=0,Type=Flag,Description=\"Phase 2 genotyped: filtered, non-redundant\">\r\n" + 
								"##INFO=<ID=PH3,Number=0,Type=Flag,Description=\"Phase 3 genotyped: filtered, non-redundant\">\r\n" + 
								"##INFO=<ID=CDA,Number=0,Type=Flag,Description=\"Variation is interrogated in a clinical diagnostic assay\">\r\n" + 
								"##INFO=<ID=LSD,Number=0,Type=Flag,Description=\"Submitted from a locus-specific database\">\r\n" + 
								"##INFO=<ID=MTP,Number=0,Type=Flag,Description=\"Microattribution/third-party annotation(TPA:GWAS,PAGE)\">\r\n" + 
								"##INFO=<ID=OM,Number=0,Type=Flag,Description=\"Has OMIM/OMIA\">\r\n" + 
								"##INFO=<ID=NOC,Number=0,Type=Flag,Description=\"Contig allele not present in SNP allele list. The reference sequence allele at the mapped position is not present in the SNP allele list, adjusted for orientation.\">\r\n" + 
								"##INFO=<ID=WTD,Number=0,Type=Flag,Description=\"Is Withdrawn by submitter If one member ss is withdrawn by submitter, then this bit is set.  If all member ss' are withdrawn, then the rs is deleted to SNPHistory\">\r\n" + 
								"##INFO=<ID=NOV,Number=0,Type=Flag,Description=\"Rs cluster has non-overlapping allele sets. True when rs set has more than 2 alleles from different submissions and these sets share no alleles in common.\">\r\n" + 
								"##INFO=<ID=GCF,Number=0,Type=Flag,Description=\"Has Genotype Conflict Same (rs, ind), different genotype.  N/N is not included.\">\r\n" +
								"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\r\n" + 
								pred.getChr()+"	"+pred.getPos()+"	.	"+pred.getRef()+"	"+pred.getAlt()+ "	.	.	.");
						fileWriter.close();
						
						Runtime.getRuntime().exec("java -jar /home/joel/Downloads/snpEff/SnpSift.jar dbnsfp -v -db /home/joel/Documentos/dbNSFP4.1a.txt.gz "+e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+".vcf"+ " > data/"+e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+".annotated.vcf");
	
						ProcessBuilder pb = new ProcessBuilder("java","-jar","/home/joel/Downloads/snpEff/SnpSift.jar",
								"dbnsfp","-v","-db","/home/joel/Documentos/dbNSFP4.1a.txt.gz", "data/"+e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+".vcf");
						pb.redirectOutput(new File("data/", e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+".annotated.vcf"));
						pb.redirectError(new File("data/", e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+"out.log"));
						Process p = pb.start();
	
						byte[] sendMessage;
						
			            InetAddress addressClient = receivePacket.getAddress();
			            int portClient = receivePacket.getPort();
			            
			            ServerResponse sr = new ServerResponse(false, "Solicitação de predição recebida!");
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								addressClient, portClient);
						serverSocket.send(sendPacket);
	
						CompletableFuture<Process> cfp = p.onExit();
						//cfp.get();
						cfp.thenAccept(
							ph_ -> 
								{
									ReversedLinesFileReader object = null;
									try {
										object = new ReversedLinesFileReader(new File("data/", e+pred.getChr()+pred.getPatient()+pred.getPos().toString()+pred.getAlt()+".annotated.vcf"));
										String result = object.readLine();
										//System.out.println(pred.getPatient()+"	" + result);
										
										File myObj = new File("data/predictions/"+e+".tsv");
										if (myObj.createNewFile()) {
											myObj.getName();
										}
	
										BufferedWriter bw = new BufferedWriter(new FileWriter("data/predictions/"+e+".tsv", true)); 
										bw.write(pred.getPatient()+"	" + result);
										bw.newLine();
										bw.close();
									} catch (IOException e1) {
										e1.printStackTrace();
								 	}finally{
										try {
											object.close();
										} catch (IOException e1) {
											e1.printStackTrace();
									  }
								 	}
								});
						
					}catch (JWTVerificationException exception){
						serverSocket.close();
					    exception.printStackTrace();
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) { 
		new UDPServerDbNSFP();
	}
}