#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

//"in" attributes from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

//declare uniforms
uniform sampler2D u_texture;
uniform float u_resolution;
uniform float u_radius;
uniform vec2 u_dir;

void main() {
    //this will be our RGBA sum
    vec4 sum = vec4(0.0);

    //our original texcoord for this fragment
    vec2 tc = v_texCoords;

    //the amount to blur, i.e. how far off center to sample from
    //1.0 -> blur by one pixel
    //2.0 -> blur by two pixels, etc.
    float blur = u_radius/u_resolution;

    //the direction of our blur
    //(1.0, 0.0) -> x-axis blur
    //(0.0, 1.0) -> y-axis blur
    float hstep = u_dir.x;
    float vstep = u_dir.y;

    //apply blurring, using a 9-tap filter with predefined gaussian weights

    vec4 s1 = texture2D(u_texture, vec2(tc.x - 4.0*blur*hstep, tc.y - 4.0*blur*vstep));
    vec4 s2 = texture2D(u_texture, vec2(tc.x - 3.0*blur*hstep, tc.y - 3.0*blur*vstep));
    vec4 s3 = texture2D(u_texture, vec2(tc.x - 2.0*blur*hstep, tc.y - 2.0*blur*vstep));
    vec4 s4 = texture2D(u_texture, vec2(tc.x - 1.0*blur*hstep, tc.y - 1.0*blur*vstep));
    vec4 s5 = texture2D(u_texture, vec2(tc.x, tc.y));
    vec4 s6 = texture2D(u_texture, vec2(tc.x + 1.0*blur*hstep, tc.y + 1.0*blur*vstep));
    vec4 s7 = texture2D(u_texture, vec2(tc.x + 2.0*blur*hstep, tc.y + 2.0*blur*vstep));
    vec4 s8 = texture2D(u_texture, vec2(tc.x + 3.0*blur*hstep, tc.y + 3.0*blur*vstep));
    vec4 s9 = texture2D(u_texture, vec2(tc.x + 4.0*blur*hstep, tc.y + 4.0*blur*vstep));

    sum += vec4(s1.rgb, s1.a) * 0.0162162162;
    sum += vec4(s2.rgb, s2.a) * 0.0540540541;
    sum += vec4(s3.rgb, s3.a) * 0.1216216216;
    sum += vec4(s4.rgb, s4.a) * 0.1945945946;

    sum += vec4(s5.rgb, s5.a) * 0.2270270270;

    sum += vec4(s6.rgb, s6.a) * 0.1945945946;
    sum += vec4(s7.rgb, s7.a) * 0.1216216216;
    sum += vec4(s8.rgb, s8.a) * 0.0540540541;
    sum += vec4(s9.rgb, s9.a) * 0.0162162162;

    //discard alpha for our simple demo, multiply by vertex color and return
    gl_FragColor = v_color * vec4(sum.rgb, 1.0);
}