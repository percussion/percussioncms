/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services.utils.xml;

import com.google.common.collect.AbstractIterator;
import com.google.common.io.Closeables;
import com.percussion.util.PSPurgableTempFile;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang.Validate.isTrue;

/**
 * Will save a stream of objects to be used later.
 * This is ideal for large query results where returning a 
 * list would be problematic and other options would break
 * the transaction boundary.
 * <p>
 * Take note that this class implements {@link Iterable}.
 * Thus the {@link #iterator()} method can be called multiple
 * times. This makes it a drop in replacement for looping
 * of collection objects.
 * 
 * @author adamgent
 *
 * @param <T>
 */
public abstract class PSObjectStream<T> implements Iterable<T>, Closeable
{
   private PSPurgableTempFile m_tempFile;
   private State m_state = State.INIT;
   private long m_size = 0;
   private PSObjectStream<?> self = this; 
   private Vector<It<?>> openIterators = new Vector<>();
   
   /**
    * State that the class can be in.
    * @author adamgent
    */
   private enum State {
      INIT,
      /**
       * The objects have been written to the tmp file.
       */
      WRITTEN,
      /**
       * The object has deleted the temp file.
       */
      DISPOSED
   }
   /**
    * See {@link ObjectOutputStream}.
    * @param writer should not be null.
    * @return not null.
    * @throws IOException
    */
   protected abstract ObjectOutputStream createObjectOutputStream(Writer writer) throws IOException;
   /**
    * See {@link ObjectInputStream}.
    * @param reader should not be null.
    * @return not null.
    * @throws IOException
    */
   protected abstract ObjectInputStream createObjectInputStream(Reader reader) throws IOException;
   
   /**
    * Creates the object stream. A temporary file will create immediatly.
    * You must call {@link #writeObjects(Iterator)} before calling
    * {@link #iterator()}.
    * @throws IOException
    */
   public PSObjectStream() throws IOException
   {
      m_tempFile = new PSPurgableTempFile("ostream", null, null);
   }

   public synchronized void close()
   {
      for (It<?> it : openIterators) {
         try {
            Closeables.close(it,false);
         } catch (IOException e) {
          //   Need to re-look at closeQuietly Structure https://github.com/google/guava/issues/1118
         }
      }
   }
   /**
    * Cleans up the resources and open streams.
    * Deletes the temp file.
    */
   public synchronized void dispose() {
      close();
      if (m_tempFile != null) {
         m_state = State.DISPOSED;
         m_tempFile.delete();
         m_tempFile = null;
      }
   }
   /**
    * Writes the objects for later use.
    * @param objects
    * @see #iterator()
    */
   public synchronized void writeObjects(Iterator<T> objects) 
   {
      isTrue(m_state == State.INIT);
      FileWriter fw = null;
      long i = 0;
      try
      {
         fw = new FileWriter(m_tempFile);
         ObjectOutputStream os = createObjectOutputStream(new BufferedWriter(fw));
         while ( objects.hasNext() ) {
            T o = objects.next();
            os.writeObject(o);
            i++;
         }
         m_state = State.WRITTEN;
         os.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         m_size = i;
         IOUtils.closeQuietly(fw);
      } 
   }
   
   public long getSize()
   {
      return m_size;
   }

   /**
    * Must call {@link #writeObjects(Iterator)} before calling
    * this method.
    * {@inheritDoc}
    */
   public Iterator<T> iterator() 
   {
      isTrue(m_state == State.WRITTEN);
      return new It<>();
   }
   
   /**
    * 
    * An iterator that reads a file deserializes objects
    * on demand.
    * @author adamgent
    *
    * @param <X>
    */
   private class It<X> extends AbstractIterator<X> implements Closeable {

      private AtomicLong m_i = new AtomicLong(m_size);
      private ObjectInputStream m_os;
      public It()
      {
         openIterators.add(this);
         FileReader fw = null;
         try 
         {
             fw = new FileReader(m_tempFile);
             m_os = createObjectInputStream(new BufferedReader(fw));
         }
         catch (IOException e)
         {
            IOUtils.closeQuietly(fw);
            throw new RuntimeException(e);
         }
      }

      @SuppressWarnings("unchecked")
      @Override
      protected X computeNext()
      {
         synchronized (self) {
            try
            {
               if (m_i.decrementAndGet() >= 0) {
                  return (X) m_os.readObject();
               }
               else {
                  IOUtils.closeQuietly(m_os);
                  return endOfData(); 
               }
            }
            catch (Exception e)
            {
               IOUtils.closeQuietly(m_os);
               throw new RuntimeException(e);
            }
         }
      }

      public void close() throws IOException
      {
         m_i.set(0);
         IOUtils.closeQuietly(m_os);
      }
   }

}
