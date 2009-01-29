//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.gpg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.Identifier;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * Wrapper around the GPG commandline tool.
 * 
 * FIXME: This should be re-implemented with a native GPG library in Java.
 */
public class LidGpg
        extends
            AbstractFactory<Identifier,LidKeyPair,Void>
        implements
            LidGpgKeyPairFactory
{
    protected static final Log log = Log.getLogInstance( LidGpg.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param nonceManager the LidNonceManager to use
     * @return the created instance
     */
    public static LidGpg create(
            LidNonceManager nonceManager )
    {
        LidGpg ret = new LidGpg( nonceManager );
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param nonceManager the LidNonceManager to use
     */
    protected LidGpg(
            LidNonceManager nonceManager )
    {
        theNonceManager = nonceManager;
    }

    /**
     * Generate private/public key pair for this LID.
     *
     * @param key the LID URL for which a key pair needs to be generated
     * @param arg ignored
     * @return the generated LidKeyPair
     * @throws FactoryException thrown if the LidKeyPair could not be created
     */
    public LidKeyPair obtainFor(
            Identifier key,
            Void       arg )
        throws
            FactoryException
    {
        try {
            // This follows the example from the gpg doc/DETAILS file
            String batchFileContent =
                      "Key-Type: DSA\n"
                    + "Key-Length: 1024\n"
                    + "Subkey-Type: ELG-E\n"
                    + "Subkey-Length: 1024\n"
                    + "Name-Real: $lid\n"
                    + "Expire-Date: 0\n"
                    + "%commit\n";

            File       batchFile       = File.createTempFile( "", "" );
            FileWriter batchFileWriter = new FileWriter( batchFile );

            batchFileWriter.write( batchFileContent );
            batchFileWriter.close();

            String command = theExecutable + " --batch --gen-key " + batchFile.getAbsolutePath();

            execute( command, null, null, null );

            batchFile.delete();

            // now construct return values

            StringBuffer output = new StringBuffer();
            command = theExecutable + " --export --armor =" + key;
            execute( command, null, output, null );

            String publicKey = output.toString();

            output = new StringBuffer();
            command = theExecutable + " --export-secret-keys --armor =" + key;
            execute( command, null, output, null );

            String privateKey = output.toString();

            LidKeyPair ret = new LidKeyPair( key, publicKey, privateKey );
            return ret;

        } catch( IOException ex ) {
            throw new FactoryException( this, ex );
        }
    }

    /**
     * Import a public key into the key store.
     *
     * @param key the public key to import
     * @throws IOException an I/O error occurred
     */
    public void importPublicKey(
            String key )
        throws
            IOException
    {
        String command = theExecutable + " --import";

        execute( command, key, null, null );
    }

    /**
     * Import a private key into the key store.
     *
     * @param key the private key to import
     * @throws IOException an I/O error occurred
     */
    public void importPrivateKey(
            String key )
        throws
            IOException
    {
        // amazingly enough, there does not seem to be a difference between importing
        // public and private keys in GPG.
        String command = theExecutable + " --import";
        StringBuffer errorData = new StringBuffer();

        execute( command, key, null, errorData );
    }

    /**
     * Reconstruct a signed message from a URL, any POST'd data and a credential.
     *
     * @param url the URL
     * @param postData the data posted to the URL using HTTP POST, if any
     * @param credential the credential
     * @return the reconstructed signed message
     */
    public String reconstructSignedMessage(
            String url,
            String postData,
            String credential )
    {
        // try argument list first
        int credentialIndex = url.indexOf( "&credential=" );
        if( credentialIndex <= 0 ) {
            credentialIndex = url.indexOf( "&lid-credential=" );
        }
        if( credentialIndex >= 0 ) {
            url = url.substring( 0, credentialIndex );
        } else if( postData != null ) {
            // try postData because argument list didn't work

            credentialIndex = postData.indexOf( "&credential=" );
            if( credentialIndex <= 0 ) {
               credentialIndex = postData.indexOf( "&lid-credential=" );
            }
            if( credentialIndex >= 0 ) {
                postData = postData.substring( 0, credentialIndex );
            }
        }

        int    crIndex             = credential.indexOf( '\n' );
        String credentialFirstLine = crIndex > 0 ? credential.substring( 0, crIndex ) : credential;

        StringBuffer signedText = new StringBuffer( 256 );
        signedText.append( "-----BEGIN PGP SIGNED MESSAGE-----\n" );
        signedText.append( "Hash: " ).append( credentialFirstLine ).append( "\n" );
        signedText.append( "\n" );
        signedText.append( url );
        if( postData != null && postData.length() > 0 ) {
            if( url.endsWith( "?" )) {
                signedText.append( '&' );
            } else {
                signedText.append( '?' );
            }
            signedText.append( postData );
        }
        signedText.append( "\n" );
        signedText.append( "-----BEGIN PGP SIGNATURE-----\n" );
        if( crIndex > 0 ) {
            signedText.append( credential.substring( crIndex+1 ));
        }

        signedText.append( "-----END PGP SIGNATURE-----\n" );

        return signedText.toString();
    }

    /**
     * Validate a piece of signed text against a LID.
     *
     * @param lid the LID URL
     * @param signedText the signed text
     * @return true if the validation was successful
     * @throws IOException an I/O error occurred
     */
    public boolean validateSignedText(
            String lid,
            String signedText )
        throws
            IOException
    {
        StringBuffer output = new StringBuffer();

        String command = theExecutable + " --verify";

        if( log.isDebugEnabled() ) {
            log.debug( "Signed text:\n" + signedText );
        }

        int result = execute( command, signedText, null, output );
        // Note that result == 0 means *good*, not bad

        if( result>0 ) {
            String stringOutput = output.toString();
            if( log.isInfoEnabled() ) {
                log.info( "Bad electronic signature on signed '" + signedText + "', output is '" + stringOutput + "'"  );
            }
            return false;
        }
        boolean ret;
        Matcher m = theLidInGpgOutputPattern.matcher( output );
        if( m.matches() ) {
            String group1 = m.group( 1 );
            if( lid.equals( group1 )) {
                ret = true;
            } else {
                ret = false;
            }
        } else {
            ret = false;
        }

        if( !ret && log.isDebugEnabled() ) {
            log.debug( "gpg --verify responded with " + output + ", but we were looking for \"" + lid + "\".\n" );
        }

        return ret;
    }
    
    /**
     * Sign a URL.
     *
     * @param lid the LID that will sign the URL
     * @param url the URL to be signed
     * @return the signed URL
     * @throws IOException an I/O error occurred
     */
    public String signUrl(
            String lid,
            String url )
        throws
            IOException
    {
        return signUrl( lid, url, null );
    }

    /**
     * Sign a URL.
     *
     * @param lid the LID that will sign the URL
     * @param url the URL to be signed
     * @param lidVersion the LID protocol version to use
     * @return the signed URL
     * @throws IOException an I/O error occurred
     */
    public String signUrl(
            String lid,
            String url,
            String lidVersion )
        throws
            IOException
    {
        StringBuffer append = new StringBuffer();
        if( url.indexOf( '?' ) >= 0 ) {
            append.append( '&' );
        } else {
            append.append( '?' );
        }

        append.append( "lid=" );
        append.append( HTTP.encodeToValidUrlArgument( lid ));
        append.append( "&lid-credtype=gpg%20--clearsign" );

        String nonce = theNonceManager.generateNewNonce();
        append.append( "&lid-nonce=" );
        append.append( HTTP.encodeToValidUrlArgument( nonce ));

        String command = theExecutable + " --clearsign -u =" + lid;
        StringBuffer output = new StringBuffer( 256 );
        execute( command, url + append, output, null );

        String outputString = output.toString();

        Matcher m = thePgpSignedPattern.matcher( outputString );
        if( m.matches() ) {
            String hash = m.group( 1 );
            String sig  = m.group( 3 );

            if( lidVersion != null && lidVersion.startsWith( "1." )) {
                append.append( "&credential=" + HTTP.encodeToValidUrlArgument( hash + "\n" + sig ));
            } else {
                append.append( "&lid-credential=" + HTTP.encodeToValidUrlArgument( hash + "\n" + sig ));
            }
            return append.toString();

        }
        throw new RuntimeException(
                "Our regular expression did not match gpg output '"
                        + outputString
                        + "', lid is '"
                        + lid
                        + "', gpg is "
                        + theExecutable
                        + ", thePgpSignedPattern is '"
                        + thePgpSignedPattern
                        + "'" );
    }

    /**
     * Export the public session key for this LID.
     *
     * @param lid the LID whose public session key we want to obtain
     * @return the public session key
     * @throws IOException an I/O error occurred
     */
    public String exportPublicSessionKey(
            String lid )
        throws
            IOException
    {
        String cmd = theExecutable + " --export --armor =" + lid;
                // need prefix = to do an exact match only

        StringBuffer output = new StringBuffer( 256 );
        /* int result = */    execute( cmd, null, output, null );

        return output.toString();
    }

    /**
     * Internal helper that knows how to execute a shell command with data that
     * we provide and that we can get back.
     *
     * This is an implementation with a lot of overhead (3 Threads plus the
     * one Thread that the JDK creates itself!) but I can't quite think of
     * anything that is reasonably simple and likely to work.
     *
     * @param command the command to execute
     * @param inputData if given, contains input data that shall be piped into command
     * @param outputData if given, command's standard output will be redirected into outputData
     * @param errorData if given, command's error output will be redirected into errorData
     * @return exit code of the child process
     * @throws IOException thrown if an input/output error occurred
     */
    protected static int execute(
            String             command,
            final String       inputData,
            final StringBuffer outputData,
            final StringBuffer errorData )
        throws
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( "about to execute: " + command );
        }

        final Process p = Runtime.getRuntime().exec( command );

        Thread inputThread  = null;
        Thread outputThread = null;
        Thread errorThread  = null;

        if( inputData != null ) {
            inputThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            OutputStreamWriter writer = new OutputStreamWriter( p.getOutputStream());
                            writer.write( inputData );
                            writer.close();
                        } catch( IOException ex ) {
                            log.error( ex );
                        }
                    }
            };
        }
        if( outputData != null ) {
            outputThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            InputStreamReader reader = new InputStreamReader( p.getInputStream());
                            int cc = 0;
                            while( ( cc = reader.read()) >= 0 ) {
                                char c = (char) cc;
                                outputData.append( c );
                            }
                        } catch( IOException ex ) {
                            log.error( ex );
                        }
                    }
            };
        }
        if( errorData != null ) {
            errorThread = new Thread() {
                    @Override
                    public void run()
                    {
                        try {
                            InputStreamReader reader = new InputStreamReader( p.getErrorStream());
                            int cc = 0;
                            while( ( cc = reader.read()) >= 0 )
                            {
                                char c = (char) cc;
                                errorData.append( c );
                            }

                        } catch( IOException ex ) {
                            log.error( ex );
                        }
                    }
            };
        }
        else if( log.isDebugEnabled() ) {
            errorThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int cc = 0;
                            InputStreamReader errorReader = new InputStreamReader( p.getErrorStream());
                            StringBuffer buf = new StringBuffer();
                            while( ( cc = errorReader.read()) >= 0  ) {
                                char c = (char) cc;
                                buf.append( c );
                                if( c == '\n' ) {
                                    log.debug( "Process reported: " + buf );
                                    buf = new StringBuffer();
                                }
                            }
                            if( buf.length() > 0 ) {
                                log.debug( "Process reported: " + buf );
                            }
                        } catch( IOException ex ) {
                            log.error( ex );
                        }
                    }
                };
        }

        if( inputThread != null ) {
            inputThread.start();
        }
        if( outputThread != null ) {
            outputThread.start();
        }
        if( errorThread != null ) {
            errorThread.start();
        }

        try {
            int returnValue = p.waitFor();

            if( inputThread != null ) {
                inputThread.join();
            }
            if( outputThread != null ) {
                outputThread.join();
            }
            if( errorThread != null ) {
                errorThread.join();
            }

            if( log.isDebugEnabled() ) {
                log.debug( "execution returned " + returnValue );
            }
            return returnValue;
        } catch( InterruptedException ex ) {
            log.error( ex );
        }
        return -1;
    }

    /**
     * The LidNonceManager to use.
     */
    protected LidNonceManager theNonceManager;
    
    /**
     * Name of the executable.
     */
    protected static String theExecutable = ResourceHelper.getInstance( LidGpg.class ).getResourceStringOrDefault( "GpgPath", "/usr/bin/gpg" );

    /**
     * The pattern in the gpg output that contains our LID.
     */
    protected static final Pattern theLidInGpgOutputPattern = Pattern.compile(
            "[\\s\\S]*\"(.*)\"[\\s\\S]*" );

    /**
     * The pattern in the gpg output that matches a signed message.
     */
    protected static final Pattern thePgpSignedPattern = Pattern.compile(
            "-----BEGIN PGP SIGNED MESSAGE-----\\s"
                    + "Hash: (\\S+)\\s\\s"
                    + "(.*)\n"
                    + "-----BEGIN PGP SIGNATURE-----\\s"
                    + "([\\s\\S]*)-----END PGP SIGNATURE-----\\s*",
            Pattern.MULTILINE | Pattern.DOTALL );
}
