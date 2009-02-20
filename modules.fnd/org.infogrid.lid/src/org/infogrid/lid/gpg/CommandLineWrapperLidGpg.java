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
import org.infogrid.util.FactoryException;
import org.infogrid.util.Identifier;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.HTTP;

/**
 * Implementation of LidGpg as wrapper around the GPG commandline tool.
 */
public class CommandLineWrapperLidGpg
        extends
            LidGpg
{
    /**
     * Factory method.
     *
     * @param nonceManager the LidNonceManager to use
     * @throws IOException GPG's home directory could not be created
     * @return the created instance
     */
    public static CommandLineWrapperLidGpg create(
            LidNonceManager nonceManager )
        throws
            IOException
    {
        CommandLineWrapperLidGpg ret = new CommandLineWrapperLidGpg( nonceManager );
        return ret;
    }

    /**
     * Constructor.
     *
     * @param nonceManager the LidNonceManager to use
     * @throws IOException GPG's home directory could not be created
     */
    protected CommandLineWrapperLidGpg(
            LidNonceManager nonceManager )
        throws
            IOException
    {
        super( nonceManager );

        // make sure the home directory exists
        if( theGpgHomeDir != null ) {
            File homeDir = new File( theGpgHomeDir );
            if( !homeDir.exists() ) {
                homeDir.mkdirs();
            } else if( !homeDir.isDirectory() ) {
                throw new IOException( "Cannot create directory " + homeDir.getAbsolutePath() );
            }
        }
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
        synchronized( SYNC ) {

            File batchFile = null;
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

                batchFile = File.createTempFile( getClass().getName(), "" );

                FileWriter batchFileWriter = new FileWriter( batchFile );

                batchFileWriter.write( batchFileContent );
                batchFileWriter.close();

                String command = theFullExecutable + " --batch --gen-key " + batchFile.getAbsolutePath();

                execute( command, null, null, null );

                // now construct return values

                StringBuffer output = new StringBuffer();
                command = theFullExecutable + " --export --armor =" + key;
                execute( command, null, output, null );

                String publicKey = output.toString();

                output = new StringBuffer();
                command = theFullExecutable + " --export-secret-keys --armor =" + key;
                execute( command, null, output, null );

                String privateKey = output.toString();

                LidKeyPair ret = new LidKeyPair( key, publicKey, privateKey );
                return ret;

            } catch( IOException ex ) {
                throw new FactoryException( this, ex );
            } finally {
                if( CLEANUP ) {
                    batchFile.delete();
                    cleanupHomedir();
                }
            }
        }
    }

    /**
     * Validate a piece of signed text against a LID.
     *
     * @param lid the LID URL
     * @param signedText the signed text
     * @param publicKey the public key to use
     * @return true if the validation was successful
     * @throws IOException an I/O error occurred
     */
    public boolean validateSignedText(
            String lid,
            String signedText,
            String publicKey )
        throws
            IOException
    {
        synchronized( SYNC ) {
            try {
                String command = theFullExecutable + " --import";

                execute( command, publicKey, null, null );

                StringBuffer output = new StringBuffer();

                command = theFullExecutable + " --verify";

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

            } finally {
                if( CLEANUP ) {
                    cleanupHomedir();
                }
            }
        }
    }

    /**
     * Sign a URL.
     *
     * @param lid the LID that will sign the URL
     * @param url the URL to be signed
     * @param privateKey the private key to use
     * @param lidVersion the LID protocol version to use
     * @return the signed URL
     * @throws IOException an I/O error occurred
     */
    public String signUrl(
            String lid,
            String url,
            String privateKey,
            String lidVersion )
        throws
            IOException
    {
        synchronized( SYNC ) {
            try {
                // amazingly enough, there does not seem to be a difference between importing
                // public and private keys in GPG.
                String command = theFullExecutable + " --import";
                StringBuffer errorData = new StringBuffer();

                execute( command, privateKey, null, errorData );

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

                command = theFullExecutable + " --clearsign -u =" + lid;
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
                                + theFullExecutable
                                + ", thePgpSignedPattern is '"
                                + thePgpSignedPattern
                                + "'" );
            } finally {
                if( CLEANUP ) {
                    cleanupHomedir();
                }
            }
        }
    }

    /**
     * Helper method to cleanup Gpg's home directory.
     * This must be invoked at the end of each operation, not at the beginning:
     * this way, it is clean upon application re-start (which may be run with
     * a different user id).
     */
    protected void cleanupHomedir()
    {
        if( theGpgHomeDir != null ) {
            File homeDir = new File( theGpgHomeDir );

            File contained [] = homeDir.listFiles();
            for( File current : contained ) {
                current.delete();
            }
        }
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
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( CommandLineWrapperLidGpg.class );

    /**
     * Name of the executable.
     */
    protected static final String theExecutable = theResourceHelper.getResourceStringOrDefault( "GpgPath", "/usr/bin/gpg" );

    /**
     * The Gpg home directory.
     */
    protected static final String theGpgHomeDir = theResourceHelper.getResourceStringOrNull( "GpgHomedir" );

    /**
     * The full-qualified executable.
     */
    protected static final String theFullExecutable = theGpgHomeDir != null ? ( theExecutable + " --homedir " + theGpgHomeDir ) : theExecutable;

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

    /**
     * If true, each operation will delete files after it is done. This is good for production.
     * Set to false in debugging situations.
     */
    protected boolean CLEANUP = theResourceHelper.getResourceBooleanOrDefault( "DeleteFiles", true );

    /**
     * Synchronization flag across all threads.
     */
    protected static final Object SYNC = new Object();
}
