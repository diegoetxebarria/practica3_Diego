package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;

public class VideoPlayer extends JFrame {
	private static final long serialVersionUID = 1L;
	static VideoPlayer miVentana;
	EmbeddedMediaPlayerComponent mediaPlayerComponent;
	EmbeddedMediaPlayer mediaPlayer;
	private JList<String> lCanciones = null;
	private JProgressBar pbVideo = null;
	private JCheckBox cbAleatorio = null;
	private JLabel lMensaje = null;
	private JLabel lMensaje2 = null;
	private JTextField tfPropTitulo = null;
	private JTextField tfPropCantante = null;
	private JTextField tfPropComentarios = null;
	JPanel pBotonera;
	JPanel pBotoneraLR;
	ArrayList<JButton> botones;
	ArrayList<JButton> botonesLR;
	JScrollPane spLCanciones;
	private ListaReproduccion listaRepVideos = new ListaReproduccion();
	private String ultimoFicheroLR = null;
	private Properties misProperties;
	private static String ultimaCarpeta = null;
	private static String ultimoPatronFicheros = null;
	private int ultimaXVentana = -1;
	private int ultimaYVentana = -1;
	private int ultimoAnchoVentana = -1;
	private int ultimoAltoVentana = -1;
	static String[] ficsBotones = new String[] { "Button Add", "Button Rewind", "Button Play Pause",
			"Button Fast Forward", "Button Maximize" };
	static String[] ficsBotonesLR = new String[] { "open", "save", "saveas", "database" };
	private DefaultListCellRenderer miListRenderer = new DefaultListCellRenderer() {
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel miComp = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (VideoPlayer.this.listaRepVideos.isErroneo(index)) {
				miComp.setForeground(Color.RED);
			}

			return miComp;
		}
	};
	private boolean controlPulsado = false;
	private static DateFormat formatoFechaLocal = DateFormat.getDateInstance(3, Locale.getDefault());
	private static DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
	private static String ficheros;
	private static String path;

	public VideoPlayer() {
		this.lCanciones = new JList(this.listaRepVideos);
		this.pbVideo = new JProgressBar(0, 10000);
		this.cbAleatorio = new JCheckBox("Rep. aleatoria");
		this.lMensaje = new JLabel("");
		this.lMensaje2 = new JLabel("");
		this.tfPropTitulo = new JTextField("", 10);
		this.tfPropCantante = new JTextField("", 10);
		this.tfPropComentarios = new JTextField("", 30);
		this.pBotonera = new JPanel();
		this.pBotoneraLR = new JPanel();
		this.botones = new ArrayList();
		String[] var4;
		int var3 = (var4 = ficsBotones).length;

		String fic;
		int var2;
		JButton boton;
		for (var2 = 0; var2 < var3; ++var2) {
			fic = var4[var2];
			boton = new JButton(new ImageIcon(VideoPlayer.class.getResource("img/" + fic + ".png")));
			this.botones.add(boton);
			boton.setName(fic);
		}

		this.botonesLR = new ArrayList();
		var3 = (var4 = ficsBotonesLR).length;

		for (var2 = 0; var2 < var3; ++var2) {
			fic = var4[var2];
			boton = new JButton(new ImageIcon(VideoPlayer.class.getResource("img/" + fic + ".png")));
			this.botonesLR.add(boton);
			boton.setName(fic);
		}

		JPanel pPropiedades = new JPanel();
		JPanel pInferior = new JPanel();
		final JPanel pIzquierda = new JPanel();
		this.mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
			private static final long serialVersionUID = 1L;

			protected FullScreenStrategy onGetFullScreenStrategy() {
				return new Win32FullScreenStrategy(VideoPlayer.this);
			}
		};
		this.mediaPlayer = this.mediaPlayerComponent.getMediaPlayer();
		int indBoton = 0;

		Iterator var6;
		for (var6 = this.botones.iterator(); var6.hasNext(); ++indBoton) {
			boton = (JButton) var6.next();
			boton.setOpaque(false);
			boton.setContentAreaFilled(false);
			boton.setBorderPainted(false);
			boton.setBorder((Border) null);
			boton.setRolloverIcon(
					new ImageIcon(VideoPlayer.class.getResource("img/" + ficsBotones[indBoton] + "-RO.png")));
			boton.setPressedIcon(
					new ImageIcon(VideoPlayer.class.getResource("img/" + ficsBotones[indBoton] + "-CL.png")));
		}

		indBoton = 0;

		for (var6 = this.botonesLR.iterator(); var6.hasNext(); ++indBoton) {
			boton = (JButton) var6.next();
			boton.setOpaque(false);
			boton.setContentAreaFilled(false);
			boton.setBorderPainted(false);
			boton.setBorder((Border) null);
		}

		this.lMensaje2.setForeground(Color.white);
		this.lMensaje2.setFont(new Font("Arial", 1, 18));
		this.setTitle("Video Player - Deusto Ingeniería");
		this.setDefaultCloseOperation(2);
		this.setSize(800, 600);
		this.lCanciones.setCellRenderer(this.miListRenderer);
		this.spLCanciones = new JScrollPane(this.lCanciones);
		this.spLCanciones.setPreferredSize(new Dimension(200, 5000));
		this.pBotonera.setLayout(new FlowLayout(0));
		pPropiedades.setLayout(new FlowLayout(0));
		pInferior.setLayout(new BorderLayout());
		pIzquierda.setLayout(new BorderLayout());
		var6 = this.botones.iterator();

		while (var6.hasNext()) {
			boton = (JButton) var6.next();
			this.pBotonera.add(boton);
		}

		var6 = this.botonesLR.iterator();

		while (var6.hasNext()) {
			boton = (JButton) var6.next();
			this.pBotoneraLR.add(boton);
		}

		this.pBotonera.add(this.lMensaje2);
		this.pBotonera.add(this.cbAleatorio);
		this.pBotonera.add(this.lMensaje);
		pPropiedades.add(new JLabel("Tit:"));
		pPropiedades.add(this.tfPropTitulo);
		pPropiedades.add(new JLabel("Cant:"));
		pPropiedades.add(this.tfPropCantante);
		pPropiedades.add(new JLabel("Coms:"));
		pPropiedades.add(this.tfPropComentarios);
		pInferior.add(pPropiedades, "North");
		pInferior.add(this.pbVideo, "South");
		pIzquierda.add(this.spLCanciones, "Center");
		pIzquierda.add(this.pBotoneraLR, "South");
		this.getContentPane().add(this.mediaPlayerComponent, "Center");
		this.getContentPane().add(this.pBotonera, "North");
		this.getContentPane().add(pInferior, "South");
		this.getContentPane().add(pIzquierda, "West");
		((JButton) this.botones.get(VideoPlayer.BotonDe.ANYADIR.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File fPath = VideoPlayer.pedirCarpeta();
				if (fPath != null) {
					VideoPlayer.path = fPath.getAbsolutePath();
					VideoPlayer.ultimaCarpeta = VideoPlayer.path;
					if (VideoPlayer.ultimoPatronFicheros == null) {
						VideoPlayer.ficheros = JOptionPane.showInputDialog((Component) null,
								"Nombre de ficheros a elegir (* para cualquier cadena)",
								"Selección de ficheros dentro de la carpeta", 3);
					} else {
						VideoPlayer.ficheros = JOptionPane.showInputDialog((Component) null,
								"Nombre de ficheros a elegir (* para cualquier cadena)",
								VideoPlayer.ultimoPatronFicheros);
					}

					VideoPlayer.ultimoPatronFicheros = VideoPlayer.ficheros;
					VideoPlayer.this.listaRepVideos.add(VideoPlayer.path, VideoPlayer.ficheros, true);
					VideoPlayer.this.lCanciones.repaint();
				}
			}
		});
		((JButton) this.botones.get(VideoPlayer.BotonDe.ATRAS.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VideoPlayer.this.paraVideo();
				if (VideoPlayer.this.cbAleatorio.isSelected()) {
					VideoPlayer.this.listaRepVideos.irARandom();
				} else {
					VideoPlayer.this.listaRepVideos.irAAnterior();
				}

				VideoPlayer.this.lanzaVideo();
			}
		});
		((JButton) this.botones.get(VideoPlayer.BotonDe.PLAY_PAUSA.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (VideoPlayer.this.mediaPlayer.isPlayable()) {
					if (VideoPlayer.this.mediaPlayer.isPlaying()) {
						VideoPlayer.this.mediaPlayer.pause();
					} else {
						VideoPlayer.this.mediaPlayer.play();
					}
				} else {
					VideoPlayer.this.lanzaVideo();
				}

			}
		});
		((JButton) this.botones.get(VideoPlayer.BotonDe.AVANCE.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VideoPlayer.this.paraVideo();
				if (VideoPlayer.this.cbAleatorio.isSelected()) {
					VideoPlayer.this.listaRepVideos.irARandom();
				} else {
					VideoPlayer.this.listaRepVideos.irASiguiente();
				}

				VideoPlayer.this.lanzaVideo();
			}
		});
		((JButton) this.botones.get(VideoPlayer.BotonDe.MAXIMIZAR.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (VideoPlayer.this.mediaPlayer.isFullScreen()) {
					VideoPlayer.this.mediaPlayer.setFullScreen(false);
					pIzquierda.setVisible(true);
					VideoPlayer.this.pBotonera.setBackground(Color.LIGHT_GRAY);
				} else {
					VideoPlayer.this.mediaPlayer.setFullScreen(true);
					pIzquierda.setVisible(false);
					VideoPlayer.this.pBotonera.setBackground(Color.BLACK);
				}

			}
		});
		this.lCanciones.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int posi = VideoPlayer.this.lCanciones.locationToIndex(e.getPoint());
					VideoPlayer.this.paraVideo();
					VideoPlayer.this.listaRepVideos.irA(posi);
					VideoPlayer.this.lanzaVideo();
				}

			}
		});
		this.pbVideo.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (VideoPlayer.this.mediaPlayer.isPlayable()) {
					float porcentajeSalto = (float) e.getX() / (float) VideoPlayer.this.pbVideo.getWidth();
					VideoPlayer.this.mediaPlayer.setPosition(porcentajeSalto);
					VideoPlayer.this.visualizaTiempoRep();
				}

			}
		});
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				VideoPlayer.this.mediaPlayer.stop();
				VideoPlayer.this.mediaPlayer.release();
				BaseDeDatos.close();
				VideoPlayer.this.salvaProperties();
			}
		});
		this.mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
			public void finished(MediaPlayer mediaPlayer) {
				VideoPlayer.this.listaRepVideos.irASiguiente();
				VideoPlayer.this.lanzaVideo();
			}

			public void error(MediaPlayer mediaPlayer) {
				VideoPlayer.this.listaRepVideos.setFicErroneo(VideoPlayer.this.listaRepVideos.getFicSeleccionado(),
						true);
				VideoPlayer.this.listaRepVideos.irASiguiente();
				VideoPlayer.this.lanzaVideo();
				VideoPlayer.this.lCanciones.repaint();
			}

			public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
				VideoPlayer.this.visualizaTiempoRep();
			}
		});
		FocusListener fl = new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				FicheroMultimedia ficVideo = VideoPlayer.this.listaRepVideos
						.getFicMM(VideoPlayer.this.listaRepVideos.getFicSeleccionado());
				if (ficVideo != null) {
					if (e.getSource() == VideoPlayer.this.tfPropTitulo) {
						ficVideo.titulo = VideoPlayer.this.tfPropTitulo.getText();
					} else if (e.getSource() == VideoPlayer.this.tfPropCantante) {
						ficVideo.cantante = VideoPlayer.this.tfPropCantante.getText();
					} else if (e.getSource() == VideoPlayer.this.tfPropComentarios) {
						ficVideo.comentarios = VideoPlayer.this.tfPropComentarios.getText();
					}
				}

			}
		};
		this.tfPropTitulo.addFocusListener(fl);
		this.tfPropCantante.addFocusListener(fl);
		this.tfPropComentarios.addFocusListener(fl);
		((JButton) this.botonesLR.get(VideoPlayer.BotonDeLR.LOAD.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File fPath = VideoPlayer.this.pedirFicheroVPD();
				if (fPath != null) {
					VideoPlayer.path = fPath.getAbsolutePath();

					try {
						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(VideoPlayer.path));
						ListaDeReproduccion lr = (ListaDeReproduccion) ois.readObject();
						ois.close();
						lr.initDataListeners();
						VideoPlayer.this.ultimoFicheroLR = VideoPlayer.path;
						VideoPlayer.this.listaRepVideos = lr;
						VideoPlayer.this.lCanciones.setModel(VideoPlayer.this.listaRepVideos);
						VideoPlayer.this.lanzaVideo();
					} catch (Exception var5) {
						var5.printStackTrace();
						JOptionPane.showMessageDialog(VideoPlayer.this,
								"El fichero " + VideoPlayer.path + " no ha podido cargarse.", "Fichero incorrecto", 0);
					}

				}
			}
		});
		((JButton) this.botonesLR.get(VideoPlayer.BotonDeLR.SAVEAS.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File fPath = VideoPlayer.this.pedirFicheroVPD();
				if (fPath != null) {
					VideoPlayer.path = fPath.getAbsolutePath();
					if (!VideoPlayer.path.toUpperCase().endsWith("VPD")) {
						VideoPlayer.path = VideoPlayer.path + ".vpd";
						fPath = new File(VideoPlayer.path);
					}

					if (fPath.exists()) {
						int conf = JOptionPane.showConfirmDialog(VideoPlayer.this,
								"¡Atención! El fichero indicado ya existe. ¿Quieres sobreescribirlo?",
								"Confirmación de fichero ya existente", 0);
						if (conf != 0) {
							return;
						}
					}

					try {
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(VideoPlayer.path));
						oos.writeObject(VideoPlayer.this.listaRepVideos);
						oos.close();
						VideoPlayer.this.ultimoFicheroLR = VideoPlayer.path;
					} catch (Exception var4) {
						var4.printStackTrace();
						JOptionPane.showMessageDialog(VideoPlayer.this,
								"El fichero " + VideoPlayer.path + " no ha podido salvarse.", "Fichero incorrecto", 0);
					}

				}
			}
		});
		((JButton) this.botonesLR.get(VideoPlayer.BotonDeLR.SAVE.ordinal())).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (VideoPlayer.this.ultimoFicheroLR == null) {
					((JButton) VideoPlayer.this.botonesLR.get(VideoPlayer.BotonDeLR.SAVEAS.ordinal())).doClick();
				} else {
					try {
						ObjectOutputStream oos = new ObjectOutputStream(
								new FileOutputStream(VideoPlayer.this.ultimoFicheroLR));
						oos.writeObject(VideoPlayer.this.listaRepVideos);
						oos.close();
					} catch (Exception var3) {
						var3.printStackTrace();
						JOptionPane.showMessageDialog(VideoPlayer.this,
								"El fichero " + VideoPlayer.this.ultimoFicheroLR + " no ha podido salvarse.",
								"Fichero incorrecto", 0);
					}

				}
			}
		});
		((JButton) this.botonesLR.get(VideoPlayer.BotonDeLR.BASEDEDATOS.ordinal()))
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (int i = 0; i < VideoPlayer.this.listaRepVideos.size(); ++i) {
							FicheroMultimedia fm = VideoPlayer.this.listaRepVideos.getFicMM(i);
							fm.anyadirFilaATabla(BaseDeDatos.getStatement());
						}

					}
				});
		this.lCanciones.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 17) {
					VideoPlayer.this.controlPulsado = false;
				} else if (e.getKeyCode() == 127) {
					int seleccionada = VideoPlayer.this.lCanciones.getSelectedIndex();
					if (seleccionada != -1) {
						if (seleccionada == VideoPlayer.this.listaRepVideos.ficheroEnCurso) {
							VideoPlayer.this.paraVideo();
							VideoPlayer.this.listaRepVideos.irASiguiente();
							VideoPlayer.this.lanzaVideo();
						}

						VideoPlayer.this.listaRepVideos.removeFic(seleccionada);
						if (seleccionada < VideoPlayer.this.listaRepVideos.size()) {
							VideoPlayer.this.lCanciones.setSelectedIndex(seleccionada);
						} else if (seleccionada > 0) {
							VideoPlayer.this.lCanciones.setSelectedIndex(seleccionada - 1);
						}
					}
				}

			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 17) {
					VideoPlayer.this.controlPulsado = true;
				} else if (VideoPlayer.this.controlPulsado) {
					Comparator<FicheroMultimedia> cfm = null;
					if (e.getKeyCode() == 78) {
						cfm = new Comparator<FicheroMultimedia>() {
							public int compare(FicheroMultimedia o1, FicheroMultimedia o2) {
								return o1.file.getName().compareTo(o2.file.getName());
							}
						};
					} else if (e.getKeyCode() == 70) {
						cfm = new Comparator<FicheroMultimedia>() {
							public int compare(FicheroMultimedia o1, FicheroMultimedia o2) {
								return (int) (o1.file.lastModified() - o2.file.lastModified());
							}
						};
					} else if (e.getKeyCode() == 84) {
						cfm = new Comparator<FicheroMultimedia>() {
							public int compare(FicheroMultimedia o1, FicheroMultimedia o2) {
								return (int) (o1.file.length() - o2.file.length());
							}
						};
					}

					VideoPlayer.this.listaRepVideos.mergeSort(cfm);
					if (VideoPlayer.this.listaRepVideos.getFicSeleccionado() != -1) {
						VideoPlayer.this.lCanciones
								.setSelectedIndex(VideoPlayer.this.listaRepVideos.getFicSeleccionado());
					}
				}

			}
		});
	}

	private void visualizaTiempoRep() {
		this.pbVideo.setValue(
				(int) (10000.0D * (double) this.mediaPlayer.getTime() / (double) this.mediaPlayer.getLength()));
		this.pbVideo.repaint();
		this.lMensaje2.setText(formatoHora.format(new Date(this.mediaPlayer.getTime() - 3600000L)));
	}

	private void paraVideo() {
		if (this.mediaPlayer != null) {
			this.mediaPlayer.stop();
		}

	}

	private void lanzaVideo() {
		if (this.mediaPlayer != null && this.listaRepVideos.getFicSeleccionado() != -1) {
			FicheroMultimedia ficVideo = this.listaRepVideos.getFicMM(this.listaRepVideos.getFicSeleccionado());
			this.mediaPlayer.playMedia(ficVideo.file.getAbsolutePath(), new String[0]);
			Date fechaVideo = new Date(ficVideo.file.lastModified());
			this.lMensaje.setText("Fecha fichero: " + formatoFechaLocal.format(fechaVideo));
			this.lMensaje.repaint();
			this.lCanciones.setSelectedIndex(this.listaRepVideos.getFicSeleccionado());
			this.lCanciones.ensureIndexIsVisible(this.listaRepVideos.getFicSeleccionado());
			this.tfPropTitulo.setText(ficVideo.titulo);
			this.tfPropCantante.setText(ficVideo.cantante);
			this.tfPropComentarios.setText(ficVideo.comentarios);
		} else {
			this.lCanciones.setSelectedIndices(new int[0]);
		}

	}

	private static File pedirCarpeta() {
		String carp = ultimaCarpeta;
		if (ultimaCarpeta == null) {
			carp = System.getProperty("user.dir");
		}

		File dirActual = new File(carp);
		JFileChooser chooser = new JFileChooser(dirActual);
		chooser.setFileSelectionMode(1);
		int returnVal = chooser.showOpenDialog((Component) null);
		return returnVal == 0 ? chooser.getSelectedFile() : null;
	}

	private File pedirFicheroVPD() {
		File dirActual = new File(System.getProperty("user.dir"));
		JFileChooser chooser = new JFileChooser(dirActual);
		chooser.setFileSelectionMode(0);
		chooser.setFileFilter(new FileNameExtensionFilter("Ficheros lista de reproducción", new String[] { "vpd" }));
		int returnVal = chooser.showOpenDialog((Component) null);
		return returnVal == 0 ? chooser.getSelectedFile() : null;
	}

	private void cargaProperties() {
		this.misProperties = new Properties();

		try {
			FileInputStream fis = new FileInputStream(new File("videoplayer.ini"));
			this.misProperties.loadFromXML(fis);
			this.ultimoFicheroLR = this.misProperties.getProperty("ultimoFicheroLR");
			ultimaCarpeta = this.misProperties.getProperty("ultimaCarpeta");
			ultimoPatronFicheros = this.misProperties.getProperty("ultimoPatronFicheros");
			this.ultimaXVentana = Integer.parseInt(this.misProperties.getProperty("ultimaXVentana"));
			this.ultimaYVentana = Integer.parseInt(this.misProperties.getProperty("ultimaYVentana"));
			this.ultimoAnchoVentana = Integer.parseInt(this.misProperties.getProperty("ultimoAnchoVentana"));
			this.ultimoAltoVentana = Integer.parseInt(this.misProperties.getProperty("ultimoAltoVentana"));
			if (this.ultimoAnchoVentana > 0 && this.ultimoAltoVentana > 0) {
				this.setSize(this.ultimoAnchoVentana, this.ultimoAltoVentana);
				this.setLocation(this.ultimaXVentana, this.ultimaYVentana);
			}

			fis.close();
		} catch (Exception var2) {
			var2.printStackTrace();
		}

	}

	private void salvaProperties() {
		try {
			PrintStream ps = new PrintStream(new File("videoplayer.ini"));
			if (this.ultimoFicheroLR != null) {
				this.misProperties.setProperty("ultimoFicheroLR", this.ultimoFicheroLR);
			}

			if (ultimaCarpeta != null) {
				this.misProperties.setProperty("ultimaCarpeta", ultimaCarpeta);
			}

			if (ultimoPatronFicheros != null) {
				this.misProperties.setProperty("ultimoPatronFicheros", ultimoPatronFicheros);
			}

			this.misProperties.setProperty("ultimaXVentana", "" + this.getX());
			this.misProperties.setProperty("ultimaYVentana", "" + this.getY());
			this.misProperties.setProperty("ultimoAnchoVentana", "" + this.getWidth());
			this.misProperties.setProperty("ultimoAltoVentana", "" + this.getHeight());
			this.misProperties.storeToXML(ps, "Video Player Deusto");
			ps.close();
		} catch (Exception var3) {
			var3.printStackTrace();
		}

	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[] { "*Pentatonix*.mp4", "test/res/" };
		}

		if (args.length < 2) {
			File fPath = pedirCarpeta();
			if (fPath == null) {
				return;
			}

			path = fPath.getAbsolutePath();
			ficheros = JOptionPane.showInputDialog((Component) null,
					"Nombre de ficheros a elegir (* para cualquier cadena)",
					"Selección de ficheros dentro de la carpeta", 3);
			ultimoPatronFicheros = ficheros;
		} else {
			ficheros = args[0];
			path = args[1];
		}

		String vlcPath = (String) System.getenv().get("vlc");
		if (vlcPath == null) {
			System.setProperty("jna.library.path", "c:\\Archivos de programa\\videolan\\vlc-2.1.5");
		} else {
			System.setProperty("jna.library.path", vlcPath);
		}

		BaseDeDatos.initBD("vpd.bd");
		BaseDeDatos.crearTablaBD();

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					VideoPlayer.miVentana = new VideoPlayer();
					VideoPlayer.miVentana.cargaProperties();
					VideoPlayer.miVentana.setVisible(true);
					VideoPlayer.miVentana.listaRepVideos.add(VideoPlayer.path, VideoPlayer.ficheros, true);
					VideoPlayer.miVentana.listaRepVideos.irAPrimero();
					VideoPlayer.miVentana.lanzaVideo();
				}
			});
		} catch (InterruptedException | InvocationTargetException var3) {
			var3.printStackTrace();
		}

	}

	static enum BotonDe {
		ANYADIR, ATRAS, PLAY_PAUSA, AVANCE, MAXIMIZAR;
	}

	static enum BotonDeLR {
		LOAD, SAVE, SAVEAS, BASEDEDATOS;
	}
}
